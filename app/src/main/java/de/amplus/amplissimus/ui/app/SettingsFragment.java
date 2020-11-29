package de.amplus.amplissimus.ui.app;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import de.amplus.amplissimus.R;
import de.amplus.amplissimus.data.Functions;
import de.amplus.amplissimus.services.DSBFetchingJobService;
import de.amplus.amplissimus.services.Prefs;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String BG_SERVICES = "background_services";
    private static final String VERSION = "version";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        try {
            PackageInfo packageInfo = getContext()
                    .getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            findPreference(VERSION).setSummary(packageInfo.versionName);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if(BG_SERVICES.equals(preference.getKey())) {
            JobScheduler scheduler =
                    (JobScheduler) requireActivity().getSystemService(JOB_SCHEDULER_SERVICE);
            if(new Prefs(requireActivity()).bgServicesOn()) {
                if(scheduler.getAllPendingJobs().size() > 0) {
                    Log.d(
                            "SettingsFragment",
                            "DSBFetchingJobService already running!"
                    );
                    return super.onPreferenceTreeClick(preference);
                }
                ComponentName componentName =
                        new ComponentName(requireActivity(), DSBFetchingJobService.class);
                JobInfo jobInfo = new JobInfo.Builder(42, componentName)
                        .setPeriodic(30 * 60 * 1000)
                        .setPersisted(true)
                        .build();
                int resCode = scheduler.schedule(jobInfo);
                if(resCode == JobScheduler.RESULT_SUCCESS) {
                    Log.d(
                            "SettingsFragment",
                            "DSBFetchingJobService scheduled! resCode: "+ resCode
                    );
                } else {
                    Log.d(
                            "SettingsFragment",
                            "Scheduling DSBFetchingJobService failed! resCode: " + resCode
                    );
                }
            } else {
                scheduler.cancel(42);
                Log.d(
                        "SettingsFragment",
                        "DSBFetchingJobService stopped!"
                );
            }
        }
        return super.onPreferenceTreeClick(preference);
    }
}