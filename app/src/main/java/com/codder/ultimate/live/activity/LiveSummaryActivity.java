package com.codder.ultimate.live.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityLiveSummaryBinding;
import com.codder.ultimate.live.model.LiveSummaryRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveSummaryActivity extends BaseActivity {

    private static final String TAG = "LiveSummaryActivity";
    private ActivityLiveSummaryBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_summary);

        if (isFinishing() || sessionManager == null || sessionManager.getUser() == null) {
            Log.e(TAG, "Session or user is null");
            finish();
            return;
        }

        setupUI();
        loadLiveSummaryData();
    }

    private void setupUI() {
        if (sessionManager.getUser().getImage() != null) {
            binding.imgUser.setUserImage(
                    sessionManager.getUser().getImage(),
                    sessionManager.getUser().getAvatarFrameImage(),
                    40
            );
        }

        // Apply background blur and crop transformation if needed
        MultiTransformation<Bitmap> transformations = new MultiTransformation<>(
                new BlurTransformation(50),
                new CenterCrop()
        );

        binding.tvName.setText(sessionManager.getUser().getName() != null ?
                sessionManager.getUser().getName() : getString(R.string.unknown));

        binding.btnHomePage.setOnClickListener(v -> onBackPressed());
    }

    private void loadLiveSummaryData() {
        Intent intent = getIntent();
        if (intent == null || intent.getStringExtra(Const.DATA) == null) {
            Log.e(TAG, "Intent or liveStreamingId is null");
            return;
        }

        String liveStreamingId = intent.getStringExtra(Const.DATA);
        if (liveStreamingId != null && liveStreamingId.isEmpty()) {
            Log.e(TAG, "liveStreamingId is empty");
            return;
        }

        fetchLiveSummaryFromServer(liveStreamingId);
    }

    private void fetchLiveSummaryFromServer(String liveStreamingId) {
        customDialogClass.show();

        Call<LiveSummaryRoot> call = RetrofitBuilder.create().getLiveSummary(liveStreamingId);
        call.enqueue(new Callback<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<LiveSummaryRoot> call, @NonNull Response<LiveSummaryRoot> response) {
                customDialogClass.dismiss();

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    LiveSummaryRoot.LiveStreamingHistory summary = response.body().getLiveStreamingHistory();
                    if (summary != null) {
                        binding.tvComments.setText(String.valueOf(summary.getComments()));
                        binding.tvDuration.setText(summary.getDuration() != null ? summary.getDuration() : "N/A");
                        binding.tvIncresedFans.setText("+" + summary.getFans());
                        binding.tvJoinedUsers.setText(String.valueOf(summary.getUser()));
                        binding.tvRcoins.setText("+" + summary.getRCoin());
                        binding.tvReceivedGifts.setText(String.valueOf(summary.getGifts()));
                    } else {
                        Log.w(TAG, "LiveStreamingHistory is null");
                    }
                } else {
                    Log.w(TAG, "Response unsuccessful or invalid body: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LiveSummaryRoot> call, @NonNull Throwable t) {
                customDialogClass.dismiss();
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
            }
        });
    }
}
