package com.codder.ultimate.leaderboard;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ActivityLeaderBoardBinding;
import com.codder.ultimate.fake.utils.FakeLeaderboardFactory;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderBoardActivity extends AppCompatActivity {

    private ActivityLeaderBoardBinding binding;

    private boolean useFakeAtRuntime = true;
    private Period period = Period.DAILY;
    private LeaderboardCategory category = LeaderboardCategory.USER;

    private SessionManager sessionManager;
    private LeaderboardAdapter adapter;

    private static final String TAG = "LeaderBoardActivity";

    private enum LeaderboardCategory {
        USER("user"),
        HOST("host"),
        AGENCY("agency");

        final String apiName;

        LeaderboardCategory(String apiName) {
            this.apiName = apiName;
        }
    }

    private enum Period {
        DAILY("daily"),
        WEEKLY("weekly"),
        MONTHLY("monthly");
        final String apiName;

        Period(String apiName) {
            this.apiName = apiName;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leader_board);
        getWindow().setFlags(512, 512);

        sessionManager = new SessionManager(this);
        adapter = new LeaderboardAdapter(this);

        binding.rvRankingList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRankingList.setAdapter(adapter);

        if (binding.rvRankingList.getItemAnimator() instanceof SimpleItemAnimator sai) {
            sai.setSupportsChangeAnimations(false);
        }

        adapter.setOnItemClickListener((item, position) -> {
            if (!useFakeAtRuntime) {
                startActivity(new Intent(this, GuestActivity.class)
                        .putExtra(Const.USERID, item.getId()));
            }
        });

        initView();
        initListeners();
        setRankListData();
    }

    private void initView() {
        setupTabs(new String[]{getString(R.string.top_users), getString(R.string.top_creators), getString(R.string.top_agency)});
        selectPeriodUI(Period.DAILY); // default selection styling
    }

    private void initListeners() {
        // Back
        binding.ivBack.setOnClickListener(v -> onBackPressed());

        // Tabs: User / Host / Agency
        binding.tablayout1.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    ImageView indicator = v.findViewById(R.id.indicator);
                    tv.setTextColor(getColorCompat(R.color.white));
                    Typeface typeface = ResourcesCompat.getFont(LeaderBoardActivity.this, R.font.airbnbcereal_w_xbd);
                    tv.setTypeface(typeface);
                    indicator.setVisibility(VISIBLE);
                }
                switch (tab.getPosition()) {
                    case 0 -> category = LeaderboardCategory.USER;
                    case 1 -> category = LeaderboardCategory.HOST;
                    case 2 -> {
                        category = LeaderboardCategory.AGENCY;
                        // Reset to daily when switching to Agency (mirrors original behavior)
                        period = Period.DAILY;
                        selectPeriodUI(period);
                    }
                    default -> category = LeaderboardCategory.USER;
                }
                setRankListData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    ImageView indicator = v.findViewById(R.id.indicator);
                    tv.setTextColor(getColorCompat(R.color.white_35));
                    Typeface typeface = ResourcesCompat.getFont(LeaderBoardActivity.this, R.font.airbnbcereal_w_bk);
                    tv.setTypeface(typeface);
                    indicator.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Period chips
        binding.tvDaily.setOnClickListener(v -> onPeriodSelected(Period.DAILY));
        binding.tvWeek.setOnClickListener(v -> onPeriodSelected(Period.WEEKLY));
        binding.tvMonth.setOnClickListener(v -> onPeriodSelected(Period.MONTHLY));
        // Lifetime is intentionally left commented to preserve original behavior.
    }

    private void onPeriodSelected(Period p) {
        period = p;
        selectPeriodUI(p);
        setRankListData();
    }

    private void selectPeriodUI(Period p) {
        // Reset all
        resetChip(binding.tvDaily);
        resetChip(binding.tvWeek);
        resetChip(binding.tvMonth);
        // Activate selected
        switch (p) {
            case DAILY -> activateChip(binding.tvDaily);
            case WEEKLY -> activateChip(binding.tvWeek);
            case MONTHLY -> activateChip(binding.tvMonth);
        }
    }

    private void resetChip(TextView chip) {
        chip.setBackground(ContextCompat.getDrawable(this, R.drawable.home_tab_unselectedbg));
        chip.setTextColor(getColorCompat(R.color.white_76));
    }


    private void activateChip(TextView chip) {
        chip.setBackground(ContextCompat.getDrawable(this, R.drawable.home_tab_selectedbg));
        chip.setTextColor(getColorCompat(R.color.white));
    }

    private int getColorCompat(int resId) {
        return ContextCompat.getColor(this, resId);
    }

    private void setupTabs(String[] titles) {

        binding.tablayout1.removeAllTabs();
        for (int i = 0; i < titles.length; i++) {
            binding.tablayout1.addTab(
                    binding.tablayout1.newTab().setCustomView(createTabView(i, titles[i]))
            );
        }

        ViewGroup tabStrip = (ViewGroup) binding.tablayout1.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            View tabView = tabStrip.getChildAt(i);
            tabView.setPadding(0, 0, 35, 0);
        }

    }

    private View createTabView(int index, String title) {
        View v = LayoutInflater.from(this).inflate(R.layout.custom_tabhorizontol_plan, null);
        TextView tv = v.findViewById(R.id.tvTab);
        ImageView indicator = v.findViewById(R.id.indicator);
        tv.setText(title);
        if (index == 0) {
            tv.setTextColor(getColorCompat(R.color.white));
            Typeface typeface = ResourcesCompat.getFont(this, R.font.airbnbcereal_w_xbd);
            tv.setTypeface(typeface);
            indicator.setVisibility(VISIBLE);
        } else {
            tv.setTextColor(getColorCompat(R.color.white_35));
            Typeface typeface = ResourcesCompat.getFont(this, R.font.airbnbcereal_w_bk);
            tv.setTypeface(typeface);
            indicator.setVisibility(INVISIBLE);
        }
        tv.setTextSize(18);
        return v;
    }

    /** Public entry that keeps adapter type in sync and fetches. */
    public void setRankListData() {
        adapter.setType(category.apiName);
        fetchLeaderboard(category, period);

    }

    /** Single place for all three API calls; keeps logic identical to original. */
    private void fetchLeaderboard(LeaderboardCategory cat, Period p) {
//        binding.progressBar.setVisibility(VISIBLE);
        binding.shimmer.setVisibility(VISIBLE);
        binding.layMain.setVisibility(GONE);

        if (useFakeAtRuntime) {
            List<LeaderboardDataRoot.DataItem> fake;
            switch (cat) {
                case USER -> fake = FakeLeaderboardFactory.makeUsers(this,30);
                case HOST -> fake = FakeLeaderboardFactory.makeHosts(30);
                case AGENCY -> fake = FakeLeaderboardFactory.makeAgencies(30);
                default -> fake = FakeLeaderboardFactory.makeUsers(this,30);
            }
//            binding.progressBar.setVisibility(GONE);
            binding.shimmer.setVisibility(GONE);
            binding.layMain.setVisibility(VISIBLE);
            setUpData(fake);
            return;
        }

        // ---- real API path ----
        final String userId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : null;
        if (userId == null) {
            Log.w(TAG, "UserId is null; showing empty state.");
            showEmptyState();
            binding.shimmer.setVisibility(GONE);
            binding.layMain.setVisibility(VISIBLE);
            return;
        }

        Call<LeaderboardDataRoot> call;
        switch (cat) {
            case USER -> call = RetrofitBuilder.create().getUserRanking(userId, p.apiName);
            case HOST -> call = RetrofitBuilder.create().getRichHost(userId, p.apiName);
            case AGENCY -> call = RetrofitBuilder.create().getRichAgency(userId, p.apiName);
            default -> call = RetrofitBuilder.create().getUserRanking(userId, p.apiName);
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LeaderboardDataRoot> c, @NonNull Response<LeaderboardDataRoot> response) {
                binding.shimmer.setVisibility(GONE);
                binding.layMain.setVisibility(VISIBLE);
                LeaderboardDataRoot body = response.body();
                List<LeaderboardDataRoot.DataItem> list = (body != null && body.getData() != null) ? body.getData() : null;
                if (list == null || list.isEmpty()) {
                    showEmptyState();
                    return;
                }
                setUpData(list);
            }

            @Override
            public void onFailure(@NonNull Call<LeaderboardDataRoot> c, @NonNull Throwable t) {
                binding.shimmer.setVisibility(GONE);
                binding.layMain.setVisibility(VISIBLE);
                Log.e(TAG, "Leaderboard fetch failed", t);
                showEmptyState();
            }
        });
    }


    private void showEmptyState() {
        binding.layFirstText.setVisibility(INVISIBLE);
        binding.laySecondText.setVisibility(INVISIBLE);
        binding.layThirdText.setVisibility(INVISIBLE);
        binding.rvRankingList.setVisibility(GONE);
        binding.noData.setVisibility(VISIBLE);
    }

    private void setTopSlotsVisibility(int count) {
        binding.layFirstText.setVisibility(count >= 1 ? VISIBLE : INVISIBLE);
        binding.laySecondText.setVisibility(count >= 2 ? VISIBLE : INVISIBLE);
        binding.layThirdText.setVisibility(count >= 3 ? VISIBLE : INVISIBLE);
    }

    private void setUpData(List<LeaderboardDataRoot.DataItem> items) {
        if (items == null || items.isEmpty()) {
            showEmptyState();
            return;
        }

        // Prepare top 3
        int topCount = Math.min(3, items.size());
        setTopSlotsVisibility(topCount);

        if (topCount >= 1) bindTopCard(
                binding.profileImage1, binding.name1, binding.tvRcoin1, binding.layFirstText, items.get(0)
        );
        if (topCount >= 2) bindTopCard(
                binding.profileImage2, binding.name2, binding.tvRcoin2, binding.laySecondText, items.get(1)
        );
        if (topCount >= 3) bindTopCard(
                binding.profileImage3, binding.name3, binding.tvRcoin3, binding.layThirdText, items.get(2)
        );

        // Remaining list for RecyclerView
        List<LeaderboardDataRoot.DataItem> rest =
                (items.size() > 3) ? new ArrayList<>(items.subList(3, items.size())) : Collections.emptyList();

        if (!rest.isEmpty()) {
            adapter.submitList(rest);
            binding.noData.setVisibility(GONE);
            binding.rvRankingList.setVisibility(VISIBLE);

            binding.rvRankingList.post(this::scrollListToTop);
        } else {
            binding.noData.setVisibility(VISIBLE);
            binding.rvRankingList.setVisibility(GONE);
        }
    }

    private void scrollListToTop() {
        binding.rvRankingList.stopScroll();
        if (binding.rvRankingList.getLayoutManager() instanceof LinearLayoutManager llm) {
            // Guarantees the first row is aligned to the top padding
            llm.scrollToPositionWithOffset(0, 0); // docs: prefer scrollToPosition for visibility; this gives exact offset.
        } else {
            binding.rvRankingList.scrollToPosition(0);
        }
    }

    @SuppressLint("SetTextI18n")
    private void bindTopCard(ImageView profileImage,
                             TextView nameView,
                             TextView valueView,
                             View container,
                             LeaderboardDataRoot.DataItem item) {

        if (item == null) {
            container.setVisibility(GONE);
            return;
        }

        container.setVisibility(VISIBLE);

        if (category == LeaderboardCategory.AGENCY) {
            if (item.getAgency() != null) {
                Glide.with(this)
                        .load(resolveImage(item.getAgency().getImage()))
                        .placeholder(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(profileImage);
                nameView.setText(item.getAgency().getName());
            }
            valueView.setText(formatNumber(item.getFinalTotalAmount()));
            container.setOnClickListener(null);
        } else {
            Glide.with(this)
                    .load(resolveImage(item.getImage()))
                    .placeholder(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(profileImage);
            nameView.setText(item.getName());
            valueView.setText(category == LeaderboardCategory.USER
                    ? formatNumber(item.getTotalSpentDiamond())
                    : formatNumber(item.getTotalEarnrCoin()));

            if (!useFakeAtRuntime) {
                container.setOnClickListener(v ->
                        startActivity(new Intent(this, GuestActivity.class)
                                .putExtra(Const.USERID, item.getId()))
                );
            }
        }
    }

    public String formatNumber(double number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000d);
        } else if (number >= 10_000) {
            return String.format("%.1fK", number / 1_000d);
        } else {
            return String.valueOf((int) number);
        }
    }

    // Add inside LeaderBoardActivity (e.g., near formatNumber)
    private Object resolveImage(Object source) {
        if (source == null) return null;
        if (source instanceof Integer) return source;
        if (source instanceof String s) {
            try {
                // If it's a number string, treat it as a drawable resId
                return Integer.valueOf(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {
                // Not a number? then it's a URL/path
                return s;
            }
        }
        return source; // fallback
    }

}