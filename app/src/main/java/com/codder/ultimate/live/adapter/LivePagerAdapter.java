package com.codder.ultimate.live.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.codder.ultimate.live.fragment.AudioLiveFragment;
import com.codder.ultimate.live.fragment.PostFragment;
import com.codder.ultimate.live.fragment.RecordReelFragment;


public class LivePagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 4;
    private final String[] categories;

    public LivePagerAdapter(@NonNull FragmentManager fm, String[] categories) {
        // Use BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT for performance
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.categories = categories != null ? categories : new String[PAGE_COUNT];
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AudioLiveFragment();
            case 1:
                return new RecordReelFragment();
            case 2:
                return new PostFragment();
            default:
                return new AudioLiveFragment();
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (categories != null && position >= 0 && position < categories.length) {
            return categories[position];
        } else {
            return "";
        }
    }
}

