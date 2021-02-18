package de.amplus.amplissimus;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import de.amplus.amplissimus.services.DSBService;
import de.amplus.amplissimus.services.Prefs;
import de.amplus.amplissimus.ui.app.MainActivity;
import de.amplus.amplissimus.ui.login.LoginActivity;

public class App extends Application {
    public static final String NEW_SUBS_CHANNEL_ID = "subs_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        Prefs prefs = new Prefs(getApplicationContext());
        if(prefs.hasCredentials()) {
            new DSBService(prefs.getUsername(), prefs.getPassword());
            if (prefs.filterPlans()) DSBService.setFilter(prefs.getGradeValue());
            DSBService.setPlans(prefs.getPlans());
            /*List<DSBService.Plan> plans = new ArrayList<>();
            List<DSBService.Substitution> subs = new ArrayList<>();
            subs.add(new DSBService.Substitution("6a", "3-4", "Schläfer", "Deutsch", "Steiner", ""));
            subs.add(new DSBService.Substitution("9bc", "1-2", "---", "Sport", "Leser", "Aufsicht Aula"));
            subs.add(new DSBService.Substitution("10a", "3", "Kreisler", "Physik", "Volt", "Arbeitsauftrag"));
            subs.add(new DSBService.Substitution("Q11", "8", "---", "1c1", "Dacher", ""));
            plans.add(new DSBService.Plan(subs,"11.1.2021 Montag", "https://abc.xyz/"));
            subs = new ArrayList<>();
            plans.add(new DSBService.Plan(subs,"12.1.2021 Dienstag", "https://abc.xyz/"));
            subs = new ArrayList<>();
            subs.add(new DSBService.Substitution("6a", "2", "---", "Mathematik", "Volt", ""));
            subs.add(new DSBService.Substitution("7b", "3-4", "Leser", "Sport", "Eichner", ""));
            subs.add(new DSBService.Substitution("8d", "1", "Steiner", "Geschichte", "Ziege", ""));
            subs.add(new DSBService.Substitution("Q12", "10-11", "Leser", "2spo2", "Eichner", ""));
            plans.add(new DSBService.Plan(subs,"13.1.2021 Mittwoch", "https://abc.xyz/"));
            DSBService.setPlans(plans);*/
        }
        setupNotificationChannels();

    }

    private void setupNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel subsChannel = new NotificationChannel(
                    NEW_SUBS_CHANNEL_ID,
                    getString(R.string.new_subs),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            subsChannel.setDescription(getString(R.string.new_subs_notification_desc));
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(subsChannel);
        }
    }
}
