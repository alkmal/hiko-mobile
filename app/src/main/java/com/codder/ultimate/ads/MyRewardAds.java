package com.codder.ultimate.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.profile.modelclass.AdsRoot;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MyRewardAds {

    private static final String TAG = "MyRewardAds ";
    private RewardedAd mRewardedAd;
    Context context;
    RewardAdListener rewardAdListener;
    SessionManager sessionManager;

    FullScreenContentCallback callBack = new FullScreenContentCallback() {
        @Override
        public void onAdShowedFullScreenContent() {
            // Called when ad is shown.
            Log.d(TAG, "Ad was shown.");
        }

        @Override
        public void onAdFailedToShowFullScreenContent(AdError adError) {
            // Called when ad fails to show.
            Log.d(TAG, "Ad failed to show.");
            rewardAdListener.onEarned();
        }

        @Override
        public void onAdDismissedFullScreenContent() {
            // Called when ad is dismissed.
            // Set the ad reference to null so you don't show the ad a second time.
            Log.d(TAG, "Ad was dismissed.");
            mRewardedAd = null;
        }
    };

    public MyRewardAds(Context context, RewardAdListener rewardAdListnear) {
        this.context = context;
        sessionManager = new SessionManager(context);
        this.rewardAdListener = rewardAdListnear;

        initGoogle();

    }

    private void initGoogle() {
        AdsRoot.Advertisement ad = sessionManager.getAds();

        if (ad == null || ad.getReward() == null || ad.getReward().isEmpty()) {
            Log.e(TAG, "Ads configuration is missing or invalid");
            return;
        }

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(context, ad.getReward(),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        mRewardedAd.setFullScreenContentCallback(callBack);
                        Log.d(TAG, "Ad was loaded.");
                    }
                });

    }

    public void showAds(Activity activity, View progressbar, LinearLayoutCompat clickableView) {
        if (mRewardedAd != null) {
            clickableView.setClickable(false);
            clickableView.setAlpha(0.5f);

            Activity activityContext = activity;
            mRewardedAd.show(activityContext, rewardItem -> {
                // Handle the reward.
                Log.d(TAG, "The user earned the reward.");
                progressbar.setVisibility(View.GONE);
                rewardAdListener.onEarned();

                clickableView.setClickable(true);
                clickableView.setAlpha(1f);
            });
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }

    public interface RewardAdListener {
        void onAdClosed();

        void onEarned();
    }

}
