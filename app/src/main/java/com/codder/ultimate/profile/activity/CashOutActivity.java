package com.codder.ultimate.profile.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityCashOutBinding;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.profile.adapter.RedeemHistoryAdapter;
import com.codder.ultimate.profile.adapter.RedeemMethodAdapter;
import com.codder.ultimate.profile.modelclass.ReedemListRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CashOutActivity extends BaseActivity {

    public static final String TAG = "CashOutActivity";
    private ActivityCashOutBinding binding;
    private List<String> paymentGateways = new ArrayList<>();
    private String selectedPaymentGateway;
    private int amount = 0;
    private int minRcoinForCashout = 0;
    private double settingCurrency = 1;
    private RedeemHistoryAdapter redeemHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cash_out);

        redeemHistoryAdapter = new RedeemHistoryAdapter();
        binding.rvHistory.setAdapter(redeemHistoryAdapter);

        setupInitialValues();
        initListeners();
        fetchRedemptionHistory();
    }

    private void setupInitialValues() {
        minRcoinForCashout = sessionManager.getSetting().getMinRcoinForCashOut();
        double userRCoin = sessionManager.getUser().getRCoin();
        double rCoinForCashOut = sessionManager.getSetting().getRCoinForCaseOut();

        binding.tvNote.setText(getString(R.string.withdrawable_rcoins) + RayziUtils.formatCoin(userRCoin));
        binding.tvSettingRcoin.setText(RayziUtils.formatCoin(rCoinForCashOut) + Const.CoinName);
        binding.tvSettingCurrency.setText(RayziUtils.formatCoin(settingCurrency) + " " + Const.getCurrency());

        paymentGateways = sessionManager.getSetting().getPaymentGateway();
        if (paymentGateways != null && !paymentGateways.isEmpty()) {
            selectedPaymentGateway = paymentGateways.get(0);
            updateGatewayHint(selectedPaymentGateway);

            RedeemMethodAdapter adapter = new RedeemMethodAdapter(this::updateGatewayHint);
            binding.rvRedeemMethods.setAdapter(adapter);
            adapter.submitList(paymentGateways);
        } else {
            Toast.makeText(this, R.string.no_payment_method_found, Toast.LENGTH_SHORT).show();
        }

        double rCoin = sessionManager.getUser().getRCoin();
        binding.tvRcoin.setText(RayziUtils.formatCoin(rCoin));
    }

    private void initListeners() {
        binding.btnSubmit.setOnClickListener(v -> submitCashOutRequest());
        binding.layoutRedeemCoin.setOnClickListener(v -> {
            binding.etRedeemCoin.requestFocus();
            binding.etRedeemCoin.setSelection(binding.etRedeemCoin.getText().length());
            showKeyboard(binding.etRedeemCoin);
        });
        binding.etRedeemCoin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateConversionInfo(s.toString());
            }
        });

    }

    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }


    private void updateConversionInfo(String value) {
        if (value.isEmpty()) {
            resetConversionFields();
            return;
        }

        try {
            amount = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return;
        }

        if (amount < minRcoinForCashout) {
            showTempNote(getString(R.string.minimum_amount_is) + " " + minRcoinForCashout + Const.CoinName);
        } else if (amount > sessionManager.getUser().getRCoin()) {
            showTempNote(getString(R.string.insufficient) + Const.CoinName);
        }

        binding.tvSettingRcoin.setText(RayziUtils.formatCoin(amount) + Const.CoinName);
        double cash = (amount * settingCurrency) / sessionManager.getSetting().getRCoinForCaseOut();
        binding.tvSettingCurrency.setText(RayziUtils.formatCoin(cash) + " " + Const.getCurrency());
    }

    private void showTempNote(String message) {
        binding.tvNote.setText(message);
        binding.tvNote.setTextColor(ContextCompat.getColor(this, R.color.red));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.tvNote.setTextColor(ContextCompat.getColor(this, R.color.yellow));
            binding.tvNote.setText(getString(R.string.withdrawable_rcoins) + sessionManager.getUser().getRCoin());
        }, 1500);
    }

    private void resetConversionFields() {
        binding.tvSettingRcoin.setText(sessionManager.getSetting().getRCoinForCaseOut() + Const.CoinName);
        binding.tvSettingCurrency.setText(RayziUtils.formatCoin(settingCurrency) + " " + Const.getCurrency());
    }

    private void submitCashOutRequest() {
        if (amount < sessionManager.getSetting().getRCoinForCaseOut()) {
            Toast.makeText(this, getString(R.string.minimum_amount_is) + " " + minRcoinForCashout + Const.CoinName, Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > sessionManager.getUser().getRCoin()) {
            Toast.makeText(this, getString(R.string.insufficient) + Const.CoinName, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPaymentGateway == null || selectedPaymentGateway.isEmpty()) {
            Toast.makeText(this, R.string.no_payment_method_found, Toast.LENGTH_SHORT).show();
            return;
        }

        String details = binding.etDetails.getText().toString().trim();
        if (details.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_your_details, Toast.LENGTH_SHORT).show();
            return;
        }

        customDialogClass.show();
        binding.btnSubmit.setEnabled(false);

        JsonObject json = new JsonObject();
        json.addProperty("userId", sessionManager.getUser().getId());
        json.addProperty("paymentGateway", selectedPaymentGateway);
        json.addProperty("description", details);
        json.addProperty("rCoin", amount);

        RetrofitBuilder.create().cashOutDiamonds(json).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                customDialogClass.dismiss();
                binding.btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(CashOutActivity.this, R.string.redeem_request_sent_successfully, Toast.LENGTH_SHORT).show();
                    updateUserBalance();
                    binding.etDetails.setText("");
                    binding.etRedeemCoin.setText("0");
                    fetchRedemptionHistory();
                } else {
                    Log.w(TAG, "Cashout request failed: " + (response.body() != null ? response.body().getMessage() : "Unknown error"));
                    Toast.makeText(CashOutActivity.this, R.string.failed_to_send_request, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                customDialogClass.dismiss();
                binding.btnSubmit.setEnabled(true);

                Log.e(TAG, "Cashout request failed: " + t.getMessage(), t);
                Toast.makeText(CashOutActivity.this, R.string.something_went_wrong_text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserBalance() {
        UserRoot.User user = sessionManager.getUser();
        user.setrCoin(user.getRCoin() - amount);
        sessionManager.saveUser(user);
    }

    private void fetchRedemptionHistory() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.shimmer.startShimmer();

        RetrofitBuilder.create().getRedeemHistory(sessionManager.getUser().getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ReedemListRoot> call, Response<ReedemListRoot> response) {
                handleShimmerAndRefreshEnd();

                if (response.isSuccessful() && response.body() != null) {
                    ReedemListRoot redeemListRoot = response.body();
                    if (redeemListRoot.isStatus() && redeemListRoot.getRedeem() != null && !redeemListRoot.getRedeem().isEmpty()) {
                        redeemHistoryAdapter.submitList(new ArrayList<>(redeemListRoot.getRedeem()));
                        binding.tvRedeemHistory.setVisibility(View.VISIBLE);
                        binding.rvHistory.setVisibility(View.VISIBLE);
                        return;
                    }
                }

                binding.tvRedeemHistory.setVisibility(View.GONE);
                binding.rvHistory.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ReedemListRoot> call, Throwable t) {
                handleShimmerAndRefreshEnd();
                binding.tvRedeemHistory.setVisibility(View.GONE);
                binding.rvHistory.setVisibility(View.GONE);
            }

            private void handleShimmerAndRefreshEnd() {
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(View.GONE);
            }
        });
    }

    private void updateGatewayHint(String method) {
        selectedPaymentGateway = method;

        int hintRes;
        switch (method.toLowerCase()) {
            case "upi details":
                hintRes = R.string.enter_your_upi_details;
                break;
            case "bybit":
                hintRes = R.string.enter_your_bybit_details;
                break;
            case "paytm details":
                hintRes = R.string.enter_your_paytm_details;
                break;
            default:
                hintRes = R.string.enter_your_bank_account_details;
                break;
        }

        binding.etDetails.setHint(hintRes);
    }
}
