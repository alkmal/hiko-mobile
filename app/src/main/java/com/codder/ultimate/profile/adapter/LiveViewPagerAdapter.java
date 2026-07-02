package com.codder.ultimate.profile.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.codder.ultimate.profile.fragment.AvatarListFragment;
import com.codder.ultimate.profile.fragment.SvgaListFragment;

public class LiveViewPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 2;
    private static final int PAGE_SVGA = 0;
    private static final int PAGE_AVATAR = 1;

    public LiveViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case PAGE_SVGA:
                return new SvgaListFragment();
            case PAGE_AVATAR:
                return new AvatarListFragment();
            default:
                return new Fragment();
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}

