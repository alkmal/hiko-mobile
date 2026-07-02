package com.codder.ultimate.profile.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityCoinSellerListBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.profile.adapter.CoinSellerListAdapter;
import com.codder.ultimate.profile.modelclass.CoinSellerRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoinSellerListActivity extends BaseActivity {

    private static final String TAG = "CoinSellerListActivity";
    private ActivityCoinSellerListBinding binding;
    private CustomDialogClass customDialogClass;
    private final CoinSellerListAdapter coinSellerListAdapter = new CoinSellerListAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_coin_seller_list);
        setupUI();
        setupListeners();
        fetchCoinSellers();
    }

    private void setupUI() {
        customDialogClass = new CustomDialogClass(this, R.style.customStyle);
        customDialogClass.setCancelable(false);
        customDialogClass.show();

        binding.rvRecharge.setAdapter(coinSellerListAdapter);
        binding.layoutNoData.setVisibility(View.GONE);
    }

    private void setupListeners() {
        coinSellerListAdapter.setOnCoinPlanClickListener(coinPlan -> {
            if (coinPlan != null) {
                openWhatsApp(coinPlan.getCountryCode(), coinPlan.getMobileNo());
            }
        });

        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> {
            coinSellerListAdapter.submitList(new ArrayList<>());
            fetchCoinSellers();
        });
    }

    private void fetchCoinSellers() {
        if (sessionManager == null || sessionManager.getUser() == null) {
            Log.e(TAG, "SessionManager or User is null.");
            showNoData();
            return;
        }

        customDialogClass.show();
        Call<CoinSellerRoot> call = RetrofitBuilder.create().getCoinSellerList(sessionManager.getUser().getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<CoinSellerRoot> call, Response<CoinSellerRoot> response) {
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
                customDialogClass.dismiss();

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<CoinSellerRoot.CoinSellerItem> sellers = response.body().getCoinSeller();
                    if (sellers != null && !sellers.isEmpty()) {
                        coinSellerListAdapter.submitList(sellers);
                        binding.layoutNoData.setVisibility(View.GONE);
                    } else {
                        showNoData();
                        Log.w(TAG, "No sellers found in response.");
                    }
                } else {
                    Log.w(TAG, "Response not successful or no sellers found.");
                    showNoData();
                }
            }

            @Override
            public void onFailure(Call<CoinSellerRoot> call, Throwable t) {
                Log.e(TAG, "Failed to fetch coin sellers: " + t.getMessage(), t);
                customDialogClass.dismiss();
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
                showNoData();
                Toast.makeText(CoinSellerListActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoData() {
        binding.layoutNoData.setVisibility(View.VISIBLE);
    }

    private void openWhatsApp(String countryCode, String mobileNumber) {
        if (countryCode == null || mobileNumber == null || countryCode.isEmpty() || mobileNumber.isEmpty()) {
            Toast.makeText(this, getString(R.string.invalid_whatsapp_number), Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = countryCode + mobileNumber;
        Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT).show();
        }
    }
}
