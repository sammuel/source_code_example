package com.photoapp.controller.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.photoapp.view.fragments.SignUpFragment;
import com.photoapp.view.fragments.TabsFragment;
import com.photoapp.R;

/**
 * Class, which helps activity with managing fragments
 */
public class FragmentHelper {


    private static FragmentManager fragmentManager;



    public FragmentHelper(FragmentManager fragmentManager) {

        this.fragmentManager = fragmentManager;
    }



    public  void changeFragment(int layoutId, Fragment fragment, boolean needAnim) {
        FragmentTransaction fragmentTransaction;
        fragmentTransaction = fragmentManager.beginTransaction();

        if (needAnim) {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right);
        }
        fragmentTransaction.replace(layoutId, fragment);
        fragmentTransaction.commit();
    }

    public   void changeFragment(int layoutId, Fragment fragment) {
        changeFragment(layoutId, fragment, true);
    }


}
