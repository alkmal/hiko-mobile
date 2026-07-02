package com.codder.ultimate.profile.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityMyIncomeBinding;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.profile.activity.CashOutActivity;
import com.codder.ultimate.profile.activity.RecordActivity;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.text.MessageFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyIncomeActivity extends BaseActivity {

    private ActivityMyIncomeBinding binding;
    private PopupBuilder popupBuilder;

    public MyIncomeActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_income);
        popupBuilder = new PopupBuilder(MyIncomeActivity.this);
        setupListeners();
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void setupListeners() {

        binding.tvMaximumWithdraw.setText("*Maximum Withdraw :" +sessionManager.getSetting().getMinRcoinForCashOut());


        binding.btnConvert.setOnClickListener(v -> {
            double currentRcoin = sessionManager.getUser() != null ? sessionManager.getUser().getRCoin() : 0;
            popupBuilder.showRcoinConvertPopup(false, currentRcoin, this::convertRcoinToDiamond);
        });

        binding.btnCashout.setOnClickListener(v -> {
            if (sessionManager.getUser() != null &&
                    sessionManager.getUser().getLevel() != null &&
                    !sessionManager.getUser().getLevel().getAccessibleFunction().isCashOut()) {
                popupBuilder.showSimplePopup(
                        getString(R.string.you_are_not_able_to_cashout_at_your_level),
                        getString(R.string.dismiss),
                        null
                );
            } else {
                startActivity(new Intent(MyIncomeActivity.this, CashOutActivity.class));
            }
        });

        binding.tvHistory.setOnClickListener(v -> startActivity(new Intent(MyIncomeActivity.this, RecordActivity.class)));
    }

    private void updateUI() {
        if (sessionManager.getUser() == null || sessionManager.getSetting() == null) return;

        double rCoin = sessionManager.getUser().getRCoin();
        double withdrawable = sessionManager.getUser().getWithdrawalRcoin();
        double conversionRate = sessionManager.getSetting().getRCoinForDiamond();

        binding.tvRcoin.setText(RayziUtils.formatCoin(rCoin));
        binding.tvWithdrawingRcoin.setText(RayziUtils.formatCoin(withdrawable));
        binding.tvSettingRcoin.setText(MessageFormat.format("{0}{1}", conversionRate, " R-Coins"));
    }

    private void convertRcoinToDiamond(int rCoin) {
        if (sessionManager.getUser() == null) return;

        customDialogClass.show();

        JsonObject request = new JsonObject();
        request.addProperty("userId", sessionManager.getUser().getId());
        request.addProperty("rCoin", rCoin);

        RetrofitBuilder.create().convertRcoinToDiamond(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserRoot> call, @NonNull Response<UserRoot> response) {
                customDialogClass.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    UserRoot userRoot = response.body();
                    if (userRoot.isStatus()) {
                        handleConversionResponse(response.body(), rCoin);
                    } else {
                        Toast.makeText(MyIncomeActivity.this, userRoot.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyIncomeActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                customDialogClass.dismiss();
                Toast.makeText(MyIncomeActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleConversionResponse(@NonNull UserRoot userRoot, int rCoin) {
        if (userRoot.isStatus() && userRoot.getUser() != null) {
            sessionManager.saveUser(userRoot.getUser());

            double conversionRate = sessionManager.getSetting().getRCoinForDiamond();
            double diamonds = rCoin / conversionRate;

            String strDiamonds = RayziUtils.formatCoin(diamonds);

            String message = getString(R.string.your) + " " + rCoin + Const.CoinName + " " +
                    getString(R.string.successfully_converted_into) + " " + strDiamonds + " " +
                    getString(R.string.diamonds);

            popupBuilder.showPopUpWithVector(R.drawable.ic_successfull, getString(R.string.conversion_successful), message, getString(R.string.continue_text), this::updateUI);
        } else {
            String message = userRoot.getMessage() != null ? userRoot.getMessage() : getString(R.string.conversion_failed);
            popupBuilder.showSimplePopup(message, getString(R.string.continue_text), this::updateUI);
        }
    }
}