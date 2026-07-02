package com.codder.ultimate.reels.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.codder.ultimate.post.fragment.FeedListFragment;
import com.codder.ultimate.reels.fragment.VideoListFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedViewPagerAdapter extends FragmentStateAdapter {
    private final List<Integer> tabTypes;
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public FeedViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Integer> tabTypes) {
        super(fragmentActivity);
        this.tabTypes = tabTypes;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (fragmentMap.containsKey(position)) {
            return fragmentMap.get(position);
        }

        Fragment fragment;
        int type = tabTypes.get(position);
        fragment = (type == 0) ? new FeedListFragment()
                : (type == 1) ? VideoListFragment.newInstance("video")
                : new Fragment();

        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return tabTypes.size();
    }

    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }
}

