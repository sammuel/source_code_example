package com.photoapp.controller.viewpager;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.photoapp.R;
import com.photoapp.view.fragments.CameraFragment;
import com.photoapp.view.fragments.SettingsFragment;

public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final int PAGE_COUNT = 2;
    private int tabTitles[] = new int[]{R.string.tab_camera, R.string.tab_settings};
    private Context context;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = CameraFragment.newInstance();
                break;
            case 1:
                fragment = SettingsFragment.newInstance();
                break;
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int tabTitleId = tabTitles[position];
        String title = context.getString(tabTitleId);
        return title;
    }
}