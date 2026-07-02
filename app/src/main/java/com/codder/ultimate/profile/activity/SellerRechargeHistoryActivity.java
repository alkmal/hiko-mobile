package com.codder.ultimate.profile.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivitySellerRechargeHistoryBinding;
import com.codder.ultimate.profile.adapter.TopUpHistoryAdapter;
import com.codder.ultimate.profile.modelclass.CoinSellerHistoryRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerRechargeHistoryActivity extends BaseActivity {

    private static final String TAG = "SellerRechargeHistoryActivity";
    private ActivitySellerRechargeHistoryBinding binding;
    private TopUpHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_seller_recharge_history);
        adapter = new TopUpHistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);

        getTopUpHistory();

        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> {
            binding.shimmer.setVisibility(View.GONE);
            binding.shimmer.stopShimmer();
            binding.rvHistory.setVisibility(View.VISIBLE);
            getTopUpHistory();
        });

    }

    private void getTopUpHistory() {
        binding.layoutNoData.setVisibility(View.GONE);
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.shimmer.startShimmer();  // Start shimmer
        binding.rvHistory.setVisibility(View.GONE);
        binding.swipeRefresh.setEnableRefresh(true);

        String userId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : null;
        if (userId == null) {
            stopShimmerAndShowEmptyState();
            return;
        }

        RetrofitBuilder.create().getTopUpHistory(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CoinSellerHistoryRoot> call, @NonNull Response<CoinSellerHistoryRoot> response) {
                binding.swipeRefresh.finishRefresh();
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    CoinSellerHistoryRoot historyRoot = response.body();

                    if (historyRoot.isStatus() && historyRoot.getHistory() != null && !historyRoot.getHistory().isEmpty()) {
                        adapter.submitList(historyRoot.getHistory());
                        binding.rvHistory.setVisibility(View.VISIBLE);
                        binding.layoutNoData.setVisibility(View.GONE);
                    } else {
                        stopShimmerAndShowEmptyState();
                    }
                } else {
                    stopShimmerAndShowEmptyState();
                    Log.e(TAG, "Failed response: HTTP " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<CoinSellerHistoryRoot> call, @NonNull Throwable t) {
                binding.swipeRefresh.finishRefresh();
                stopShimmerAndShowEmptyState();
                Log.e(TAG, "Network error: " + t.getLocalizedMessage(), t);
            }
        });
    }

    private void stopShimmerAndShowEmptyState() {
        binding.shimmer.stopShimmer();
        binding.shimmer.setVisibility(View.GONE);
        binding.rvHistory.setVisibility(View.GONE);
        binding.layoutNoData.setVisibility(View.VISIBLE);
    }

}
