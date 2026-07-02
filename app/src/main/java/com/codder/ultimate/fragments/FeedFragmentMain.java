package com.codder.ultimate.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.MainActivity;
import com.codder.ultimate.databinding.FragmentFeedMainBinding;
import com.codder.ultimate.profile.activity.ProfileActivity;
import com.codder.ultimate.reels.adapter.FeedViewPagerAdapter;
import com.codder.ultimate.reels.fragment.VideoListFragment;
import com.codder.ultimate.reels.utils.MyExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.ArrayList;
import java.util.List;

public class FeedFragmentMain extends BaseFragment {
    private static final String TAG = "FeedFragmentMain";

    private FragmentFeedMainBinding binding;
    private List<TabItem> tabItems;
    private int currentPage = 0;

    private static final int FEED_PAGE = 0;
    private static final int VIDEO_PAGE = 1;

    public FeedFragmentMain() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed_main, container, false);
        initializeTabs();
        setupDynamicTabs();
        setupViewPager();
        setupListeners();

        if (tabItems.get(currentPage).type != VIDEO_PAGE) {
            pausePlayer();
        }

        return binding.getRoot();
    }

    private void initializeTabs() {
        tabItems = new ArrayList<>();
        tabItems.add(new TabItem(getString(R.string.feed), FEED_PAGE));
        tabItems.add(new TabItem(getString(R.string.video), VIDEO_PAGE));
    }

    private void setupDynamicTabs() {
        LinearLayout tabContainer = binding.tabContainer;
        tabContainer.removeAllViews();

        for (int i = 0; i < tabItems.size(); i++) {
            View tabView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab_layout, tabContainer, false);
            TextView tabTitle = tabView.findViewById(R.id.tvTabTitle);
            View tabIndicator = tabView.findViewById(R.id.tabIndicator);
            Typeface typeface;

            if (i == currentPage){
                typeface = ResourcesCompat.getFont(requireActivity(), R.font.airbnbcereal_w_xbd);
            }else {
                typeface = ResourcesCompat.getFont(requireActivity(), R.font.airbnbcereal_w_bk);
            }

            tabTitle.setTypeface(typeface);
            tabTitle.setText(tabItems.get(i).title);
            tabTitle.setTextColor(i == currentPage ? getResources().getColor(R.color.white) : getResources().getColor(R.color.white_35));
            tabIndicator.setVisibility(i == currentPage ? View.VISIBLE : View.INVISIBLE);

            int finalI = i;
            tabView.setOnClickListener(v -> {
                currentPage = finalI;
                binding.viewPager.setCurrentItem(currentPage);
                updateTabUI();
            });

            tabContainer.addView(tabView);
        }
    }

    private void setupViewPager() {
        List<Integer> tabTypes = new ArrayList<>();
        for (TabItem tab : tabItems) tabTypes.add(tab.type);

        binding.viewPager.setAdapter(new FeedViewPagerAdapter(requireActivity(), tabTypes));
        binding.viewPager.setUserInputEnabled(false);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateTabUI();

                Fragment fragment = ((FeedViewPagerAdapter) binding.viewPager.getAdapter()).getFragment(position);

                if (position == VIDEO_PAGE && fragment instanceof VideoListFragment) {
                    ((VideoListFragment) fragment).resumeVideo();
                } else {
                    pausePlayer();
                }
            }
        });

        binding.viewPager.setCurrentItem(currentPage);
        updateTabUI();
    }

    private void setupListeners() {
        binding.ivProfile.setOnClickListener(view -> {
            MyExoPlayer.getInstance().stopAndReleasePlayer();
            ((MainActivity) getActivity()).openProfileFragment();
        });
    }

    private void updateTabUI() {
        LinearLayout tabContainer = binding.tabContainer;

        for (int i = 0; i < tabItems.size(); i++) {
            View tabView = tabContainer.getChildAt(i);
            TextView tabTitle = tabView.findViewById(R.id.tvTabTitle);
            View tabIndicator = tabView.findViewById(R.id.tabIndicator);

            Typeface typeface;

            if (i == currentPage){
                typeface = ResourcesCompat.getFont(requireActivity(), R.font.airbnbcereal_w_xbd);
            }else {
                typeface = ResourcesCompat.getFont(requireActivity(), R.font.airbnbcereal_w_bk);
            }

            tabTitle.setTypeface(typeface);
            tabTitle.setTextColor(i == currentPage ? getResources().getColor(R.color.white) : getResources().getColor(R.color.white_35));
            tabIndicator.setVisibility(i == currentPage ? View.VISIBLE : View.INVISIBLE);
        }

        int currentType = tabItems.get(currentPage).type;
        binding.ivProfile.setVisibility(currentType == FEED_PAGE ? View.VISIBLE : View.INVISIBLE);

        toggleVideoPlayback(currentType);
    }


    private void toggleVideoPlayback(int type) {
        SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(requireContext());
        if (player != null) {
            player.setPlayWhenReady(type == VIDEO_PAGE);
        }
    }

    private void pausePlayer() {
        SimpleExoPlayer player = MyExoPlayer.getInstance().getPlayer(requireContext());
        if (player != null) player.setPlayWhenReady(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager.getUser() != null) {
            binding.ivProfile.setHomeUserImage(
                    sessionManager.getUser().getImage(),
                    sessionManager.getUser().getAvatarFrameImage(),
                    15
            );
        }

        int currentType = tabItems.get(currentPage).type;
        if (currentType == VIDEO_PAGE) {
            Fragment fragment = ((FeedViewPagerAdapter) binding.viewPager.getAdapter()).getFragment(currentPage);
            if (fragment instanceof VideoListFragment) {
                ((VideoListFragment) fragment).resumeVideo();
            }
            toggleVideoPlayback(currentType);
        } else {
            pausePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Fragment view destroyed");
    }

    private static class TabItem {
        String title;
        int type;

        TabItem(String title, int type) {
            this.title = title;
            this.type = type;
        }
    }
}
