package com.photoapp.view.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.photoapp.controller.fragments.FragmentHelper;
import com.photoapp.R;
import com.photoapp.view.fragments.settings.NetworkSettingsFragment;
import com.photoapp.view.fragments.settings.UploadListFragment;
import com.photoapp.view.fragments.settings.UploadStatFragment;


public class SettingsFragment extends BaseFragment {

    private static final String LOG_TAG = SettingsFragment.class.getCanonicalName();
    private static final int UPLOAD_STAT_LAYOUT_ID = R.id.layout_fragment_upload_stat;
    private static final int UPLOAD_LIST_LAYOUT_ID = R.id.layout_fragment_upload_list;
    private static final int NETWORK_SETTINGS_LAYOUT_ID = R.id.layout_fragment_network_settings;
    private FragmentHelper fragmentHelper;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();
        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();

        fragmentHelper = new FragmentHelper(supportFragmentManager);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initNetworkSetings(view);

        initUploadFileList(view);

        initStats();
        
    }

    private void initStats() {
        Fragment fragment = UploadStatFragment.newInstance();
        fragmentHelper.changeFragment(UPLOAD_STAT_LAYOUT_ID, fragment);
    }

    private void initUploadFileList(View view) {
        Fragment fragment = UploadListFragment.newInstance();
        fragmentHelper.changeFragment(UPLOAD_LIST_LAYOUT_ID, fragment);
    }

    private void initNetworkSetings(View view) {
        Fragment fragment = NetworkSettingsFragment.newInstance();
        fragmentHelper.changeFragment(NETWORK_SETTINGS_LAYOUT_ID, fragment);
    }


}
