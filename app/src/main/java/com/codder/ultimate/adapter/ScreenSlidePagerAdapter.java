package com.codder.ultimate.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.codder.ultimate.fragments.FeedFragmentMain;
import com.codder.ultimate.fragments.LiveFragmentMain;
import com.codder.ultimate.fragments.MessageFragment;
import com.codder.ultimate.fragments.ProfileFragment;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {
    public ScreenSlidePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new LiveFragmentMain();
        } else if (position == 1) {
            return new FeedFragmentMain();
        } else if (position == 2) {
            return new MessageFragment();
        } else if (position == 3) {
            return new ProfileFragment();
        } else {
            return new LiveFragmentMain();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}