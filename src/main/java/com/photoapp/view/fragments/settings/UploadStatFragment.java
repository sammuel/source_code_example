package com.photoapp.view.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.photoapp.MyApplication;
import com.photoapp.R;
import com.photoapp.controller.events.PictureFilesUpdatedEvent;
import com.photoapp.controller.thread.UIThreadHelper;
import com.photoapp.model.database.DAOHelper;
import com.photoapp.view.fragments.BaseFragment;


public class UploadStatFragment extends BaseFragment {

    private static final String LOG_TAG = UploadStatFragment.class.getCanonicalName();

    private TextView takenToday;
    private TextView takenAllTime;
    private TextView uploadedToday;
    private TextView uploadedAllTime;



    public static UploadStatFragment newInstance() {
        UploadStatFragment fragment = new UploadStatFragment();
        return fragment;
    }

    public UploadStatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshInfo();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload_stat, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        takenToday = (TextView) view.findViewById(R.id.text_taken_today);
        takenAllTime = (TextView) view.findViewById(R.id.text_taken_all_time);
        uploadedToday = (TextView) view.findViewById(R.id.text_uploaded_today);
        uploadedAllTime = (TextView) view.findViewById(R.id.text_uploaded_all_time);

        refreshInfo();
    }

    public void onEvent(PictureFilesUpdatedEvent event) {
        refreshInfo();
    }

    private void refreshInfo() {

        UIThreadHelper.executeInUIThread(new Runnable() {
            @Override
            public void run() {
                DAOHelper daoHelper = MyApplication.instance.getDaoHelper();

                int todayTaken = daoHelper.getTodayTaken();
                int allTimeTaken = daoHelper.getAllTimeTaken();
                int todayUploaded = daoHelper.getTodayUploaded();
                int allTimeUploaded = daoHelper.getAllTimeUploaded();

                String todayTakenStr = String.valueOf(todayTaken);
                String allTimeTakenStr = String.valueOf(allTimeTaken);
                String todayUploadedStr = String.valueOf(todayUploaded);
                String allTimeUploadedStr = String.valueOf(allTimeUploaded);

                takenToday.setText(todayTakenStr);
                takenAllTime.setText(allTimeTakenStr);
                uploadedToday.setText(todayUploadedStr);
                uploadedAllTime.setText(allTimeUploadedStr);
            }
        });
    }
}
