package com.codder.ultimate.chat.activity;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.activity.FakeAudioCallActivity;
import com.codder.ultimate.chat.modelclass.ChatUserListRoot;
import com.codder.ultimate.databinding.ActivityFakeCallRequestBinding;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.retrofit.Const;
import com.google.gson.Gson;

public class FakeCallRequestActivity extends BaseActivity {

    private ActivityFakeCallRequestBinding binding;
    private final Handler handler = new Handler();
    private int secondsElapsed = 0;
    private String videoLink;
    private MediaPlayer mediaPlayer;

    private final Runnable callProgressRunnable = new Runnable() {
        @Override
        public void run() {
            secondsElapsed++;
            if (secondsElapsed >= 3) {
                binding.tvStatus.setText(R.string.ringing);
            }

            if (secondsElapsed >= 5) {
                launchFakeVideoCall();
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fake_call_request);
        parseIntentData();
        handler.postDelayed(callProgressRunnable, 1000);

        binding.btnDecline.setOnClickListener(v -> onBackPressed());
    }

    private void parseIntentData() {
        Intent intent = getIntent();
        boolean isFromRandom = intent.getBooleanExtra(Const.IS_FROM_RANDOM, false);

        String userStr = isFromRandom
                ? intent.getStringExtra(Const.USER)
                : intent.getStringExtra(Const.CHATROOM);

        if (userStr == null || userStr.isEmpty()) {
            Log.e("FakeCallRequestActivity", "Invalid user data");
            finish();
            return;
        }

        if (isFromRandom) {
            GuestProfileRoot.User guestUser = new Gson().fromJson(userStr, GuestProfileRoot.User.class);
            bindUserDetails(guestUser.getName(), guestUser.getImage(), guestUser.getAvatarFrameImage());
            videoLink = guestUser.getLink();
        } else {
            ChatUserListRoot.ChatUserItem chatUser = new Gson().fromJson(userStr, ChatUserListRoot.ChatUserItem.class);
            bindUserDetails(chatUser.getName(), chatUser.getImage(), chatUser.getAvatarFrameImage());
            videoLink = chatUser.getLink();
        }

        startRingtone();
    }

    private void bindUserDetails(String name, String imageUrl, String frameImage) {
        binding.tvName.setText(name);
        binding.imgUser.setUserImage(imageUrl, frameImage, 30);

        Glide.with(FakeCallRequestActivity.this)
                .load(imageUrl)
                .into(binding.backBlurImage);
    }

    private void launchFakeVideoCall() {
        handler.removeCallbacks(callProgressRunnable);
        Intent intent = new Intent(this, FakeAudioCallActivity.class);
//        intent.putExtra(Const.VIDEO_LINK, videoLink);

        if (getIntent().getBooleanExtra(Const.IS_FROM_RANDOM, false)) {
            String guestUserJson = getIntent().getStringExtra(Const.USER);
            intent.putExtra(Const.USER, guestUserJson);
            intent.putExtra(Const.IS_FROM_RANDOM, true);
        } else {
            String chatUserJson = getIntent().getStringExtra(Const.CHATROOM);
            intent.putExtra(Const.USER, chatUserJson);
            intent.putExtra(Const.IS_FROM_RANDOM, false);
        }

        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(callProgressRunnable);
    }

    @Override
    public void onBackPressed() {
        handler.removeCallbacks(callProgressRunnable);
        stopRingtone();
        super.onBackPressed();
    }

    private void startRingtone() {
        stopRingtone(); // In case it's already playing
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone1);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void stopRingtone() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
    }
}
