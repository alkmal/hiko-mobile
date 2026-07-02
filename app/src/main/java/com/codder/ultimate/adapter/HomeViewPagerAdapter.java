package com.codder.ultimate.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.codder.ultimate.fragments.LiveListFragment;

public class HomeViewPagerAdapter extends FragmentStatePagerAdapter {

    private final String[] categories;

    public HomeViewPagerAdapter(@NonNull FragmentManager fm, @NonNull String[] categories) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.categories = categories != null ? categories : new String[0];
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position >= 0 && position < categories.length) {
            return LiveListFragment.newInstance(categories[position]);
        } else {
            return new Fragment();
        }
    }

    @Override
    public int getCount() {
        return categories.length;
    }
}
