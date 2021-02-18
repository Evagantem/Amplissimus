package de.amplus.amplissimus.ui.app;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import de.amplus.amplissimus.R;
import de.amplus.amplissimus.services.DSBFetchingJobService;
import de.amplus.amplissimus.services.DSBService;
import de.amplus.amplissimus.services.Prefs;
import de.amplus.amplissimus.ui.login.LoginActivity;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String BG_SERVICES = "background_services";
    public static final String FILTER_PLANS = "filter_plans";
    public static final String VERSION = "version";
    public static final String LOGOUT = "logout";
    public static final String GRADE = "grade";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        try {
            PackageInfo packageInfo = requireContext()
                    .getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            findPreference(VERSION).setSummary(packageInfo.versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        EditTextPreference gradePreference = findPreference(GRADE);
        gradePreference.setOnPreferenceChangeListener(this::onGradeChanged);
        gradePreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
        onGradeChanged(gradePreference, new Prefs(requireActivity()).getGradeValue());

        SwitchPreferenceCompat filterPlansSwitchPreferenceCompat = findPreference(FILTER_PLANS);
        filterPlansSwitchPreferenceCompat.setOnPreferenceChangeListener(this::onFilterPlansChanged);
    }

    private boolean onGradeChanged(Preference preference, Object newValue) {
        SwitchPreferenceCompat filterPlansPreference = findPreference(FILTER_PLANS);
        if (((String) newValue).trim().isEmpty()) {
            DSBService.setFilter(null);
            preference.setSummary(getString(R.string.grade_preference_desc));
            filterPlansPreference.setChecked(false);
            filterPlansPreference.setEnabled(false);
        } else {
            if (filterPlansPreference.isChecked()) DSBService.setFilter(((String) newValue).trim());
            preference.setSummary((String) newValue);
            filterPlansPreference.setEnabled(true);
        }
        ((MainActivity) requireActivity()).updatePlansList();
        return true;
    }

    private boolean onFilterPlansChanged(Preference preference, Object newValue) {
        Boolean value = (Boolean) newValue;
        if (value) {
            DSBService.setFilter(((EditTextPreference) findPreference(GRADE)).getText().trim());
        } else {
            DSBService.setFilter(null);
        }
        ((MainActivity) requireActivity()).updatePlansList();
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (BG_SERVICES.equals(preference.getKey())) {
            JobScheduler scheduler =
                    (JobScheduler) requireActivity().getSystemService(JOB_SCHEDULER_SERVICE);
            if (new Prefs(requireActivity()).bgServicesOn()) {
                if (scheduler.getAllPendingJobs().size() > 0) {
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
                if (resCode == JobScheduler.RESULT_SUCCESS) {
                    Log.d(
                            "SettingsFragment",
                            "DSBFetchingJobService scheduled! resCode: " + resCode
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
        } else if (LOGOUT.equals(preference.getKey())) {
            Intent intent = new Intent(requireActivity(), LoginActivity.class).putExtra("redirect", false);
            startActivity(intent);
            requireActivity().finish();
        }
        return super.onPreferenceTreeClick(preference);
    }
}