package com.codder.ultimate.profile.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.MyLoader;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityHistoryBinding;
import com.codder.ultimate.profile.adapter.CoinHistoryAdapter;
import com.codder.ultimate.profile.modelclass.CustomDate;
import com.codder.ultimate.profile.modelclass.HistoryListRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends BaseActivity {

    private ActivityHistoryBinding binding;
    private final CoinHistoryAdapter coinHistoryAdapter = new CoinHistoryAdapter();
    private CustomDate startDate;
    private CustomDate endDate;
    private String coinType = "";
    private int start = 0;
    private final MyLoader myLoader = new MyLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_history);
            binding.setMyLoder(myLoader);

            initializeIntentData();
            setupUI();
            setupDatePickers();
            getRecordData(false, false);

            binding.swipeRefresh.setOnRefreshListener(refreshLayout -> {
                getRecordData(false, true);
            });

            binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> {
                getRecordData(true, false);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            coinType = intent.getStringExtra(Const.TYPE);
            String startJson = intent.getStringExtra(Const.START_DATE);
            String endJson = intent.getStringExtra(Const.END_DATE);

            if (startJson != null && endJson != null) {
                startDate = new Gson().fromJson(startJson, CustomDate.class);
                endDate = new Gson().fromJson(endJson, CustomDate.class);
            }
        }
    }

    private void setupUI() {
        if (coinType != null && coinType.equals(Const.RCOINS)) {
            binding.topLayout.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.purple));
            binding.tvTitle.setText("R-Coins");
            binding.layAllcoin.setBackground(ContextCompat.getDrawable(this,R.drawable.bg_rcoin_history));
        }

        binding.rvHistory.setAdapter(coinHistoryAdapter);
        coinHistoryAdapter.setCoinType(coinType);

        if (startDate != null) {
            binding.tvDate1.setText(startDate.getDateForHuman());
        }

        if (endDate != null) {
            binding.tvDate2.setText(endDate.getDateForHuman());
        }

        binding.ivBack.setOnClickListener(v -> onBackPressed());

        binding.rvHistory.post(() -> {
            int screenHeight = binding.rvHistory.getRootView().getHeight();
            binding.rvHistory.setMinimumHeight(screenHeight);
        });

    }

    private void setupDatePickers() {
        binding.lytDate1.setOnClickListener(view -> showDatePicker(true));
        binding.lytDate2.setOnClickListener(view -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    CustomDate selectedDate = new CustomDate(dayOfMonth, month + 1, year);
                    if (isStartDate) {
                        startDate = selectedDate;
                        binding.tvDate1.setText(selectedDate.getDateForHuman());
                    } else {
                        endDate = selectedDate;
                        binding.tvDate2.setText(selectedDate.getDateForHuman());
                    }
                    getRecordData(false, true);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void getRecordData(boolean isLoadMore, boolean isRefreshing) {
        if (startDate == null || endDate == null) return;

        if (!isLoadMore && !isRefreshing) {
            myLoader.isFirstTimeLoading.set(true);
        }

        if (!isLoadMore) {
            start = 0;
        } else {
            start += Const.LIMIT;
        }

        myLoader.noData.set(false);

        Call<HistoryListRoot> call = RetrofitBuilder.create().getCoinHistory(
                sessionManager.getUser().getId(),
                startDate.getDateForServer(),
                endDate.getDateForServer(),
                coinType,
                start,
                Const.LIMIT
        );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<HistoryListRoot> call, @NonNull Response<HistoryListRoot> response) {
                myLoader.isFirstTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();

                if (response.isSuccessful() && response.body() != null) {
                    HistoryListRoot result = response.body();
                    if (result.isStatus() && result.getHistory() != null && !result.getHistory().isEmpty()) {

                        List<HistoryListRoot.HistoryItem> filtered = new ArrayList<>();
                        for (HistoryListRoot.HistoryItem item : result.getHistory()) {
                            if (coinType.equals(Const.DIAMOND) && item.getDiamond() != 0)
                                filtered.add(item);
                            else if (!coinType.equals(Const.DIAMOND) && item.getRCoin() != 0)
                                filtered.add(item);
                        }

                        coinHistoryAdapter.submitList(filtered);
                        binding.tvIncome.setText(RayziUtils.formatCoin(result.getIncomeTotal()));
                        binding.tvOutcome.setText(RayziUtils.formatCoin(result.getOutgoingTotal()));
                    } else if (start == 0) {
                        myLoader.noData.set(true);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistoryListRoot> call, @NonNull Throwable t) {
                myLoader.isFirstTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
                t.printStackTrace();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
