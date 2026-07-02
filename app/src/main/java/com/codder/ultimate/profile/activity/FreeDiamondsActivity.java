package com.codder.ultimate.profile.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.ads.MyRewardAds;
import com.codder.ultimate.databinding.ActivityFreeDiamondsBinding;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FreeDiamondsActivity extends BaseActivity implements MyRewardAds.RewardAdListener {

    private static final String TAG = "FreeDiamondsActivity";

    private ActivityFreeDiamondsBinding binding;
    private MyRewardAds myRewardAds;
    private boolean isAdShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_free_diamonds);

        initGradientText();
        setupUI();
        initRewardAds();
    }

    private void setupUI() {
        if (sessionManager == null || sessionManager.getUser() == null) return;

        binding.tvCode.setText(sessionManager.getUser().getReferralCode());
        binding.tvReferralCount.setText(getString(R.string.you_have) + sessionManager.getUser().getReferralCount() + getString(R.string.referrals));
        binding.tvReferString.setText(getString(R.string.invite_desc) + "" + sessionManager.getSetting().getReferralBonus() + "" + getString(R.string._diamonds));


        binding.lytAds.setOnClickListener(v -> watchVideoClick());

    }

    private void initGradientText() {

        Shader shader = new LinearGradient(
                0, 0,                      // start (LEFT)
                binding.watchVideo.getLineHeight(), 0,              // end (RIGHT)
                new int[]{
                        getResources().getColor(R.color.free_diamond_text1),
                        getResources().getColor(R.color.free_diamond_text2)
                },
                null,
                Shader.TileMode.CLAMP
        );


        binding.watchVideo.getPaint().setShader(shader);
        binding.tvRefer.getPaint().setShader(shader);
    }

    private void initRewardAds() {
        myRewardAds = new MyRewardAds(this, this);
    }


    private void watchVideoClick() {
        if (isAdShowing) return;

        isAdShowing = true;
        binding.progressBarAds.setVisibility(View.VISIBLE);
        deactivateButton();

        if (sessionManager.getUser().getAd().getCount() < sessionManager.getSetting().getMaxAdPerDay()) {
            new Handler().postDelayed(() -> {
                myRewardAds.showAds(FreeDiamondsActivity.this, binding.progressBarAds, binding.lytAds);
            }, 3000);
        } else {
            Toast.makeText(this, getString(R.string.you_exceed_your_ad_limit), Toast.LENGTH_SHORT).show();
            deactivateButton();
            binding.progressBarAds.setVisibility(View.GONE);
            isAdShowing = false;
        }
    }


    private void deactivateButton() {
        binding.lytAds.setClickable(false);
        binding.lytAds.setAlpha(0.5f);
        binding.tvWatchVideo.setTextColor(getResources().getColor(R.color.shimmerColor));
        binding.tvWatchVideo.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.shimmerColor)));

    }

    private void activateButton() {
        binding.lytAds.setClickable(true);
        binding.lytAds.setAlpha(1f);
        binding.tvWatchVideo.setTextColor(getResources().getColor(R.color.white));
        binding.tvWatchVideo.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
    }

    public void onClickCopy(View view) {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (manager != null) {
            ClipData clipData = ClipData.newPlainText("referral_code", binding.tvCode.getText());
            manager.setPrimaryClip(clipData);
            Toast.makeText(this, R.string.referral_code_copied, Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickShare(View view) {
        try {
            String shareMessage = getString(R.string.let_me_recommend_you_this_application);
            shareMessage = shareMessage + getString(R.string.share_message_template) + BuildConfig.APPLICATION_ID + "\n\n";
            shareMessage = shareMessage + getString(R.string.here_is_my_referral_code) + sessionManager.getUser().getReferralCode().toUpperCase(Locale.ROOT);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_one)));
        } catch (Exception e) {
            Log.e(TAG, "Share error: ", e);
        }
    }

    public void onClickSubmit(View view) {
        String referCode = binding.etReferralCode.getText().toString().trim();
        if (referCode.isEmpty()) {
            Toast.makeText(this, R.string.enter_refer_code, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.tvSubmit.setEnabled(false);

        JsonObject payload = new JsonObject();
        payload.addProperty("userId", sessionManager.getUser().getId());
        payload.addProperty("referralCode", referCode);

        RetrofitBuilder.create().redeemReferralCode(payload).enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(@NonNull Call<UserRoot> call, @NonNull Response<UserRoot> response) {
                binding.tvSubmit.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        Toast.makeText(FreeDiamondsActivity.this, R.string.refereed_successfully, Toast.LENGTH_SHORT).show();
                        sessionManager.saveUser(response.body().getUser());

                        binding.etReferralCode.setText("");
                    } else {
                        String errorMessage = response.body().getMessage() != null
                                ? response.body().getMessage()
                                : getString(R.string.generic_error);
                        Toast.makeText(FreeDiamondsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Referral redemption failed: " + errorMessage);
                    }
                } else {
                    Log.e(TAG, "Referral submit failed: Response unsuccessful.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                binding.tvSubmit.setEnabled(true);
                Log.e(TAG, "Referral submit failed: ", t);
                Toast.makeText(FreeDiamondsActivity.this, R.string.something_went_wrong_text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAdClosed() {
        initRewardAds();
        binding.progressBarAds.setVisibility(View.GONE);
        activateButton();
        isAdShowing = false;
    }

    @Override
    public void onEarned() {
        initRewardAds();
        rewardUserForAd();
        binding.progressBarAds.setVisibility(View.GONE);
        activateButton();
        isAdShowing = false;
    }

    private void rewardUserForAd() {
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", sessionManager.getUser().getId());

        RetrofitBuilder.create().addDiamondFromAds(payload).enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(@NonNull Call<UserRoot> call, @NonNull Response<UserRoot> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    sessionManager.saveUser(response.body().getUser());
                    Toast.makeText(FreeDiamondsActivity.this, R.string.earned_by_user, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FreeDiamondsActivity.this, R.string.something_went_wrong_text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserRoot> call, @NonNull Throwable t) {
                Log.e(TAG, "Ad reward failed: ", t);
                Toast.makeText(FreeDiamondsActivity.this, R.string.something_went_wrong_text, Toast.LENGTH_SHORT).show();

            }
        });
    }

}
