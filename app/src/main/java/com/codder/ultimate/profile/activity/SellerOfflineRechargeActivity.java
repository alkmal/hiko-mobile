package com.codder.ultimate.profile.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivitySellerOfflineRechargeBinding;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.GuestUsersListRoot;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.profile.modelclass.CoinSellerDataRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerOfflineRechargeActivity extends BaseActivity {
    private ActivitySellerOfflineRechargeBinding binding;
    private CoinSellerDataRoot.CoinSeller coinSeller;
    private boolean isUserIdVerified = false;
    private static final String TAG = "SellerOfflineRechargeActivity";

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable verifyRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_seller_offline_recharge);

        initViews();
        setupListeners();
        fetchMyCoinSellerData();
    }

    private void initViews() {

        binding.tvHistory.setOnClickListener(v -> {
            Intent intent = new Intent(SellerOfflineRechargeActivity.this, SellerRechargeHistoryActivity.class);
            startActivity(intent);
        });

        binding.ivFind.setVisibility(GONE);

        binding.ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {

        binding.btnTopup.setOnClickListener(v -> {
            if (isDemoVersion) {
                new PopupBuilder(this).showSimplePopup(getString(R.string.this_is_a_demo_version), getString(R.string.ok), () -> {
                });
            } else {
                performCoinTopUp();
            }
        });

        binding.etCoin.addTextChangedListener(inputWatcher);
        binding.etUserId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userId = binding.etUserId.getText().toString().trim();

                binding.tvUserName.setVisibility(GONE);
                isUserIdVerified = false;

                if (userId.length() < 8) {
                    return;
                }

                if (verifyRunnable != null) handler.removeCallbacks(verifyRunnable);

                if (verifyRunnable != null)
                    handler.removeCallbacks(verifyRunnable);

                verifyRunnable = () -> {
                    String id = binding.etUserId.getText().toString().trim();
                    if (!id.isEmpty())
                        if (id.length() == 8) {
                            verifyUserIdLive(id);
                        }

                };

                handler.postDelayed(verifyRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private void verifyUserIdLive(String enteredUserId) {

        String myUserId = sessionManager.getUser().getUniqueId();
        if (myUserId != null && myUserId.equals(enteredUserId)) {
            showUserName(sessionManager.getUser().getName());
            isUserIdVerified = true;
            return;
        }

        fetchUserById(enteredUserId);
    }


    private void showUserName(String name) {
        binding.tvUserName.setVisibility(VISIBLE);
        binding.tvUserName.setText(name != null ? name : getString(R.string.user_not_found));
        if (name != null){
            binding.ivFind.setVisibility(VISIBLE);
        }else {
            binding.ivFind.setVisibility(GONE);
        }

        validateInputs();
    }

    private void fetchUserById(String userId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("value", userId);
        jsonObject.addProperty("start", 0);
        jsonObject.addProperty("limit", Const.LIMIT);

        RetrofitBuilder.create().searchUser(jsonObject).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GuestUsersListRoot> call, Response<GuestUsersListRoot> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isStatus()
                        && response.body().getUser() != null
                        && !response.body().getUser().isEmpty()) {

                    isUserIdVerified = true;
                    showUserName(response.body().getUser().get(0).getName());
                    binding.ivFind.setVisibility(VISIBLE);
                } else {
                    isUserIdVerified = false;
                    showUserName(null);
                    binding.ivFind.setVisibility(GONE);
                }
            }

            @Override
            public void onFailure(Call<GuestUsersListRoot> call, Throwable t) {
                isUserIdVerified = false;
                showUserName(null);
                binding.ivFind.setVisibility(GONE);
            }
        });
    }



    private void fetchMyCoinSellerData() {
        binding.shimmer.setVisibility(VISIBLE);
        binding.shimmer.startShimmer();
        binding.tvMyCoins.setVisibility(GONE);

        String userId = sessionManager.getUser().getId();

        if (userId == null) {
            customDialogClass.dismiss();
            Toast.makeText(this, R.string.user_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitBuilder.create().getMyCoinSellerData(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CoinSellerDataRoot> call, @NonNull Response<CoinSellerDataRoot> response) {
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(GONE);
                binding.tvMyCoins.setVisibility(VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    coinSeller = response.body().getCoinSeller();
                    if (coinSeller != null) {
                        binding.tvMyCoins.setText(RayziUtils.formatCoin(coinSeller.getCoin()));
                        setupListeners();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CoinSellerDataRoot> call, @NonNull Throwable t) {
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(GONE);
                binding.tvMyCoins.setVisibility(VISIBLE);

                Log.e(TAG, "Coin seller data fetch failed", t);
            }
        });
    }

    private void performCoinTopUp() {
        if (!isUserIdVerified) {
            Toast.makeText(this, R.string.please_verify_user_id, Toast.LENGTH_SHORT).show();
            return;
        }

        String enteredUserId = binding.etUserId.getText().toString().trim();
        String enteredCoins = binding.etCoin.getText().toString().trim();
        String enteredNote = binding.etNote.getText().toString().trim();

        if (enteredUserId.isEmpty()) {
            Toast.makeText(this, R.string.enter_user_id, Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredUserId.length() != 8){
            Toast.makeText(this, "please enter valid userId", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredCoins.isEmpty()) {
            Toast.makeText(this, R.string.enter_coin_value, Toast.LENGTH_SHORT).show();
            return;
        }

        int coinValue;
        try {
            coinValue = Integer.parseInt(enteredCoins);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_coin_value, Toast.LENGTH_SHORT).show();
            return;
        }

        if (coinSeller == null || coinSeller.getCoin() < coinValue) {
            Toast.makeText(this, R.string.insufficient_coins, Toast.LENGTH_SHORT).show();
            return;
        }

        sendCoinsToUser(enteredUserId, coinValue, enteredNote);
    }

    private void sendCoinsToUser(String userId, int coins, String note) {
        customDialogClass.show();

        JsonObject json = new JsonObject();
        json.addProperty("coinSellerId", coinSeller.getId());
        json.addProperty("uniqueId", userId);
        json.addProperty("coin", coins);
        json.addProperty("note", note != null ? note : "");

        RetrofitBuilder.create().sendCoinToUser(json).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CoinSellerDataRoot> call, @NonNull Response<CoinSellerDataRoot> response) {
                if (customDialogClass != null) customDialogClass.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    CoinSellerDataRoot result = response.body();
                    if (result.isStatus()) {
                        Toast.makeText(SellerOfflineRechargeActivity.this, R.string.send_successfully, Toast.LENGTH_SHORT).show();
                        clearInputFields();

                        coinSeller = result.getCoinSeller();
                        if (coinSeller != null) {
                            binding.tvMyCoins.setText(RayziUtils.formatCoin(coinSeller.getCoin()));
                            Log.d(TAG, "Updated coin balance: " + coinSeller.getCoin());
                        }
                    } else {
                        Log.w(TAG, "Coin send failed: " + result.getMessage());
                        Toast.makeText(SellerOfflineRechargeActivity.this, result.getMessage() != null ? result.getMessage() : getString(R.string.transaction_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Coin send response error: " + response.code() + " - " + response.message());
                    Toast.makeText(SellerOfflineRechargeActivity.this, getString(R.string.transaction_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CoinSellerDataRoot> call, @NonNull Throwable t) {
                if (customDialogClass != null) customDialogClass.dismiss();
                Log.e(TAG, "Coin send failed: " + t.getLocalizedMessage(), t);
                Toast.makeText(SellerOfflineRechargeActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearInputFields() {
        binding.etUserId.setText("");
        binding.etCoin.setText("");
        binding.etNote.setText("");
        binding.tvUserName.setVisibility(GONE);
        isUserIdVerified = false;
    }

    private final TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateInputs();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void validateInputs() {
        String coinText = binding.etCoin.getText().toString().trim();
        boolean isCoinValid = !coinText.isEmpty() && coinText.matches("\\d+");
    }

}