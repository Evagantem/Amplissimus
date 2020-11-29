package de.amplus.amplissimus.ui.app;


import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import de.amplus.amplissimus.R;
import de.amplus.amplissimus.services.DSBFetchingJobService;
import de.amplus.amplissimus.services.DSBService;
import de.amplus.amplissimus.services.Prefs;

public class MainActivity extends AppCompatActivity {

    private boolean portableSession;
    public boolean isPortableSession() {
        return portableSession;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if(intent.getStringExtra("username") != null) {
            new DSBService(
                    intent.getStringExtra("username"),
                    intent.getStringExtra("password")
            );
            DSBService.setPlans(new Prefs(this).getPlans());
        }

        if(new Prefs(this).bgServicesOn()) startScheduledJob();

        portableSession = getIntent().getBooleanExtra("portable", true);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = findViewById(R.id.tab_view_pager);
        viewPager.setAdapter(new MainAppPagerAdapter(
                getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                this
        ));
        ((TabLayout) findViewById(R.id.tab_layout)).setupWithViewPager(viewPager);
    }

    private void startScheduledJob() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if(scheduler.getAllPendingJobs().size() > 0) {
            Log.d(
                    "MainActivity",
                    "DSBFetchingJobService already running!"
            );
            return;
        }
        ComponentName componentName = new ComponentName(this, DSBFetchingJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(42, componentName)
                .setPeriodic(30 * 60 * 1000)
                .setPersisted(true)
                .build();
        int resCode = scheduler.schedule(jobInfo);
        if(resCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(
                    "MainActivity",
                    "DSBFetchingJobService scheduled! resCode: "+ resCode
            );
        } else {
            Log.d(
                    "MainActivity",
                    "Scheduling DSBFetchingJobService failed! resCode: " + resCode
            );
        }
    }
}

