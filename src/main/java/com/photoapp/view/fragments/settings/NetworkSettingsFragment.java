package com.photoapp.view.fragments.settings;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.photoapp.MyApplication;
import com.photoapp.R;
import com.photoapp.model.database.DAOHelper;
import com.photoapp.model.upload.UploadService;
import com.photoapp.view.fragments.BaseFragment;


public class NetworkSettingsFragment extends BaseFragment {

    private static final String LOG_TAG = NetworkSettingsFragment.class.getCanonicalName();


    private SwitchCompat wifiSwitch;

    public static NetworkSettingsFragment newInstance() {
        NetworkSettingsFragment fragment = new NetworkSettingsFragment();
        return fragment;
    }

    public NetworkSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        return inflater.inflate(R.layout.fragment_network_settings, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initSwitch(view);

    }



    private void initSwitch(View view) {

        final DAOHelper daoHelper = MyApplication.instance.getDaoHelper();

        wifiSwitch = (SwitchCompat) view.findViewById(R.id.switch_wifi_only);

        wifiSwitch.setChecked(daoHelper.getWifiOnlyState());
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                daoHelper.setWifiOnlyState(isChecked);
                UploadService.startService(getActivity());
            }
        });
    }


}
