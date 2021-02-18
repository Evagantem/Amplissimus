package de.amplus.amplissimus.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

import de.amplus.amplissimus.App;
import de.amplus.amplissimus.R;
import de.amplus.amplissimus.data.Functions;
import de.amplus.amplissimus.ui.app.MainActivity;
import de.sematre.dsbmobile.DSBMobile;

public class DSBFetchingJobService extends JobService {

    private Thread thread;
    private JobParameters params;

    private String plansToString(List<DSBService.Plan> plans) {
        StringBuilder message = new StringBuilder("für ");
        for(DSBService.Plan plan : plans) {
            if(plans.indexOf(plan) == plans.size() - 1)
                message.append(String.format(" und %s", plan.getDay()));
            else if(plans.indexOf(plan) == 0)
                message.append(plan.getDay());
            else message.append(String.format(", %s", plan.getDay()));
        }
        return message.toString();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("DSBFetchingJobService", "DSBFetchingJobService started!");
        this.params = params;
        Prefs prefs = new Prefs(this);
        if(prefs.getUsername() == null || prefs.getPassword() == null) return false;
        thread = new Thread(() -> {
            if (!Functions.isOnline(this)) {
                finish(false);
                return;
            }
            try {
                DSBService dsbService = new DSBService(prefs.getUsername(), prefs.getPassword());
                if (prefs.getPlans() != null) {
                    List<DSBMobile.TimeTable> timeTables =
                            new DSBMobile(prefs.getUsername(), prefs.getPassword()).getTimeTables();
                    if (prefs.getPlans().get(0).getUrl().equals(timeTables.get(0).getDetail()))
                        return;
                }
                planParsingToNotification(dsbService);
                finish(false);
            } catch (Exception e) {
                finish(false);
                e.printStackTrace();
            }
        });
        thread.start();
        return true;
    }
    private void planParsingToNotification(DSBService dsbService) {
        try {
            List<DSBService.Plan> plans = dsbService.parseTimetables();
            if (plans.isEmpty()) return;
            new Prefs(this).setPlans(plans);
            sendNotification(plansToString(plans));
        } catch (Exception ignored) { finish(true); }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if(thread.isAlive())
            try {
                thread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return true;
    }

    private void finish(boolean reschedule) {
        Log.d("DSBFetchingJobService", "DSBFetchingJobService finished!");
        jobFinished(params, reschedule);
    }

    private void sendNotification(String message) {
        Prefs prefs = new Prefs(this);
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra("username", prefs.getUsername())
                .putExtra("password", prefs.getPassword());
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this,0, intent, 0);

        Notification notification = new NotificationCompat.Builder(
                this,
                App.NEW_SUBS_CHANNEL_ID
        ).setContentTitle(getString(R.string.new_subs))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(this).notify(1, notification);
    }
}