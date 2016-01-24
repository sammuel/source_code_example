package com.photoapp.view.fragments;

import android.support.v4.app.Fragment;

import com.photoapp.controller.events.BaseEvent;
import com.photoapp.controller.events.SignUpEvent;
import com.photoapp.model.events.Bus;


public class BaseFragment extends Fragment {


    @Override
    public void onResume() {
        super.onResume();
        Bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Bus.unregister(this);
    }


    public void onEvent(BaseEvent event) {
    }
}
