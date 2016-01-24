package com.photoapp.view.fragments;


import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.photoapp.R;
import com.photoapp.controller.viewpager.SampleFragmentPagerAdapter;

public class TabsFragment extends Fragment {


    public static TabsFragment newInstance() {
        TabsFragment fragment = new TabsFragment();
        return fragment;
    }

    public TabsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tabs, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager viewPager = initViewPager(view);
        InitTabs(view, viewPager);


    }

    private void InitTabs(View view, ViewPager viewPager) {
        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @NonNull
    private ViewPager initViewPager(View view) {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        FragmentActivity activity = getActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        SampleFragmentPagerAdapter sampleFragmentPagerAdapter = new SampleFragmentPagerAdapter(fragmentManager, activity);
        viewPager.setAdapter(sampleFragmentPagerAdapter);
        return viewPager;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
