package de.amplus.amplissimus.ui.app;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import de.amplus.amplissimus.R;

public class MainAppPagerAdapter extends FragmentStatePagerAdapter {

    public MainAppPagerAdapter(@NonNull FragmentManager fm, int behavior, Activity activity) {
        super(fm, behavior);
        this.settings = activity.getString(R.string.settings);
        this.subs = activity.getString(R.string.subs);
    }

    private final String settings;
    private final String subs;

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new SubsFragment();
            case 1: return new SettingsFragment();
            default: return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return subs;
            case 1: return settings;
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
