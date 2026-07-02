package com.codder.ultimate.profile.activity;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityStoreBinding;
import com.codder.ultimate.profile.adapter.LiveViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class StoreActivity extends BaseActivity {

    private ActivityStoreBinding binding;
    private final List<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_store);

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.viewPager.setAdapter(new LiveViewPagerAdapter(getSupportFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        categories.add(getString(R.string.admission_car));
        categories.add(getString(R.string.avatar_frame));

        setupTabs(categories);
        customizeTabBehavior();
    }

    private void setupListeners() {
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void setupTabs(@NonNull List<String> titles) {
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabLayout.removeAllTabs();

        for (int i = 0; i < titles.size(); i++) {
            TabLayout.Tab newTab = binding.tabLayout.newTab();
            newTab.setCustomView(createTabView(i, titles.get(i)));
            binding.tabLayout.addTab(newTab);
        }

        TabLayout.Tab defaultTab = binding.tabLayout.getTabAt(0);
        if (defaultTab != null) defaultTab.select();

    }

    private View createTabView(int position, @NonNull String title) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_categories_live_tab, null);
        TextView tabTextView = view.findViewById(R.id.tvPaymentName);
        View indicator = view.findViewById(R.id.indicator);
        tabTextView.setText(title);

        // Initial style
        if (position == 0) {
            tabTextView.setTextColor(ContextCompat.getColor(this, R.color.white));
            tabTextView.setTypeface(getTabFont(true));
            indicator.setVisibility(VISIBLE);
        } else {
            tabTextView.setTextColor(ContextCompat.getColor(this, R.color.white_35));
            tabTextView.setTypeface(getTabFont(false));
            indicator.setVisibility(INVISIBLE);
        }

        return view;
    }

    private void customizeTabBehavior() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                updateTabViewStyle(tab, true);
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {
                updateTabViewStyle(tab, false);
            }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
            }
        });
    }

    private void updateTabViewStyle(@NonNull TabLayout.Tab tab, boolean isSelected) {
        View customView = tab.getCustomView();
        if (customView == null) return;

        TextView textView = customView.findViewById(R.id.tvPaymentName);
        View indicator = customView.findViewById(R.id.indicator);
        if (textView != null) {
            if (isSelected) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                textView.setTypeface(getTabFont(true));
                indicator.setVisibility(VISIBLE);
            } else {
                textView.setTextColor(ContextCompat.getColor(this, R.color.white_35));
                textView.setTypeface(getTabFont(false));
                indicator.setVisibility(INVISIBLE);
            }
        }
    }

    private @Nullable Typeface getTabFont(boolean isBold) {
        int fontRes = isBold ? R.font.airbnbcereal_w_xbd : R.font.airbnbcereal_w_bk;
        try {
            return ResourcesCompat.getFont(this, fontRes);
        } catch (Exception e) {
            Log.e(TAG, "getTabFont: ", e);
            return Typeface.DEFAULT;
        }
    }
}