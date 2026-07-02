package com.codder.ultimate.guestuser.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.codder.ultimate.guestuser.fragment.GuestUserPostsFragment;
import com.codder.ultimate.guestuser.fragment.GuestUserReelsFragment;
import com.codder.ultimate.modelclass.GuestProfileRoot;


public class GuestUserProfileViewPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "GuestUserProfileViewPagerAdapter";
    private GuestProfileRoot.User user;

    public GuestUserProfileViewPagerAdapter(FragmentManager fm, GuestProfileRoot.User userDummy) {
        super(fm);
        this.user = userDummy;

        Log.d(TAG, "GuestUserProfileViewPagerAdapter: " +user.getId());
        Log.d(TAG, "GuestUserProfileViewPagerAdapter: " +user.isFake());
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return GuestUserPostsFragment.newInstance(user);
        } else {
            return GuestUserReelsFragment.newInstance(user);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
