package com.codder.ultimate.profile.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityRecordBinding;
import com.codder.ultimate.profile.modelclass.CustomDate;
import com.codder.ultimate.profile.modelclass.CoinRecordRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordActivity extends BaseActivity {

    private ActivityRecordBinding binding;
    private CustomDate startDate;
    private CustomDate endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_record);
            initializeDates();
            setupListeners();
            getRecordData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeDates() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        int year = calendar.get(Calendar.YEAR);

        startDate = new CustomDate(day, month, year);
        endDate = new CustomDate(day, month, year);

        binding.tvDate1.setText(startDate.getDateForHuman());
        binding.tvDate2.setText(endDate.getDateForHuman());
    }

    private void setupListeners() {
        binding.lytDiamonds.setOnClickListener(v -> launchHistory(Const.DIAMOND));
        binding.lytRcoins.setOnClickListener(v -> launchHistory(Const.RCOINS));
        binding.ivBack.setOnClickListener(v -> onBackPressed());

        binding.lytDate1.setOnClickListener(view -> showDatePickerDialog(true));
        binding.lytDate2.setOnClickListener(view -> showDatePickerDialog(false));
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int initialYear = calendar.get(Calendar.YEAR);
        int initialMonth = calendar.get(Calendar.MONTH);
        int initialDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            CustomDate selectedDate = new CustomDate(dayOfMonth, month + 1, year);
            if (isStartDate) {
                startDate = selectedDate;
                binding.tvDate1.setText(startDate.getDateForHuman());
            } else {
                endDate = selectedDate;
                binding.tvDate2.setText(endDate.getDateForHuman());
            }
            getRecordData();
        }, initialYear, initialMonth, initialDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void launchHistory(@NonNull String type) {
        if (startDate == null || endDate == null) return;

        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(Const.TYPE, type);
        intent.putExtra(Const.START_DATE, new Gson().toJson(startDate));
        intent.putExtra(Const.END_DATE, new Gson().toJson(endDate));
        startActivity(intent);
    }

    private void getRecordData() {
        if (startDate == null || endDate == null) return;

        clearTextViews();
        binding.shimmer.setVisibility(View.VISIBLE);

        Call<CoinRecordRoot> call = RetrofitBuilder.create()
                .getCoinRecord(
                        sessionManager.getUser().getId(),
                        startDate.getDateForServer(),
                        endDate.getDateForServer()
                );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CoinRecordRoot> call, @NonNull Response<CoinRecordRoot> response) {
                binding.shimmer.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    CoinRecordRoot root = response.body();

                    if (root.getDiamond() != null) {
                        binding.tvDimondsIncome.setText(RayziUtils.formatCoin(root.getDiamond().getIncome()));
                        binding.tvDiamondsOutcome.setText(RayziUtils.formatCoin(root.getDiamond().getOutgoing()));
                    }

                    if (root.getRCoin() != null) {
                        binding.tvRcoinIncome.setText(RayziUtils.formatCoin(root.getRCoin().getIncome()));
                        binding.tvRcoinOutcome.setText(RayziUtils.formatCoin(root.getRCoin().getOutgoing()));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CoinRecordRoot> call, @NonNull Throwable t) {
                binding.shimmer.setVisibility(View.GONE);
                t.printStackTrace();
                Toast.makeText(RecordActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearTextViews() {
        binding.tvDimondsIncome.setText("0");
        binding.tvDiamondsOutcome.setText("0");
        binding.tvRcoinIncome.setText("0");
        binding.tvRcoinOutcome.setText("0");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}