package com.photoapp.view.fragments.settings;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.photoapp.MyApplication;
import com.photoapp.R;
import com.photoapp.controller.events.PictureFilesUpdatedEvent;
import com.photoapp.controller.list.UploadListAdapter;
import com.photoapp.controller.thread.UIThreadHelper;
import com.photoapp.model.database.DAO.PictureFileDao;
import com.photoapp.model.database.DAOHelper;
import com.photoapp.view.fragments.BaseFragment;


public class UploadListFragment extends BaseFragment {

    private static final String LOG_TAG = UploadListFragment.class.getCanonicalName();


    private UploadListAdapter uploadListAdapter;
    private ListView uploadListView;

    public static UploadListFragment newInstance() {
        UploadListFragment fragment = new UploadListFragment();
        return fragment;
    }

    public UploadListFragment() {
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
        return inflater.inflate(R.layout.fragment_upload_list, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initListView(view);

        
    }

    private void initListView(View view) {


        uploadListView = (ListView) view.findViewById(R.id.list_upload_files);
        Activity activity = getActivity();



        DAOHelper daoHelper = MyApplication.instance.getDaoHelper();

        Cursor cursor = daoHelper.createUploadListCursor();
        uploadListAdapter = new UploadListAdapter(activity, cursor);
        uploadListView.setAdapter(uploadListAdapter);

        LinearLayout emptyView = (LinearLayout) view.findViewById(R.id.list_upload_files_empty);
        uploadListView.setEmptyView(emptyView);
    }


    public void onEvent(PictureFilesUpdatedEvent event) {
        refreshInfo();
    }

    private void refreshInfo() {

        new AsyncTask<Void, Void,Void>() {
            Cursor newCursor;

            @Override
            protected Void doInBackground(Void... params) {
                DAOHelper daoHelper = MyApplication.instance.getDaoHelper();
                newCursor = daoHelper.createUploadListCursor();

                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Cursor oldCursor = uploadListAdapter.swapCursor(newCursor);
                uploadListAdapter.notifyDataSetChanged();

                if(oldCursor != null) {
                    oldCursor.close();
                }

            }

        }.execute();


    }
}
