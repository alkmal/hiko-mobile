package com.codder.ultimate.activity;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.codder.ultimate.R;
import com.codder.ultimate.chat.modelclass.ChatUserListRoot;
import com.codder.ultimate.databinding.ActivityFakeAudioCallBinding;
import com.codder.ultimate.databinding.ActivityFakeAudioWatchBinding;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.retrofit.Const;
import com.google.gson.Gson;

import java.util.Locale;
import java.util.Random;

public class FakeAudioCallActivity extends BaseActivity {

    ActivityFakeAudioCallBinding binding;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long startTime = 0;
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private boolean isSpeakerOn = false;
    private boolean mMuted;
    private SensorManager sensorManager;

    private Sensor proximitySensor;
    private PowerManager.WakeLock wakeLock;


    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_fake_audio_call);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(false);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        handleIntentData();
        binding.btnDecline.setOnClickListener(v -> finish());

        binding.ivSpeaker.setOnClickListener(v -> {

            isSpeakerOn = !isSpeakerOn;

            if (isSpeakerOn) {

                // Loud Speaker
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audioManager.setSpeakerphoneOn(true);

                binding.ivSpeaker.setImageResource(R.drawable.ic_call_speaker);

            } else {

                // Phone Earpiece
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audioManager.setSpeakerphoneOn(false);

                binding.ivSpeaker.setImageResource(R.drawable.ic_call_speackeroff);
            }
        });

        binding.btnMute.setOnClickListener(v -> {
            mMuted = !mMuted;
            int res = mMuted ? R.drawable.ic_call_mute : R.drawable.ic_call_unmute;
            binding.btnMute.setImageResource(res);
        });

    }

    private void bindUserDetails(String name, String imageUrl, String frameImage) {
        binding.tvName.setText(name);
        binding.imgMainProfile.setUserImage(imageUrl, frameImage, 30);
        Glide.with(this).load(imageUrl).into(binding.backBlurImage);
    }

    private void handleIntentData() {
//        videoURL = getIntent().getStringExtra(Const.VIDEO_LINK);
//
//        Uri sourceUri = (videoURL == null || videoURL.isEmpty())
//                ? defaultRawUri
//                : Uri.parse(videoURL);
//
//        if (videoURL == null || videoURL.isEmpty()) {
//            Toast.makeText(this, R.string.please_try_after_some_time, Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

        String userJson = getIntent().getStringExtra(Const.USER);
        boolean isFromRandom = getIntent().getBooleanExtra(Const.IS_FROM_RANDOM, false);

        if (userJson != null) {
            if (isFromRandom) {
                GuestProfileRoot.User guestUser = new Gson().fromJson(userJson, GuestProfileRoot.User.class);
                bindUserDetails(guestUser.getName(), guestUser.getImage(), guestUser.getAvatarFrameImage());
            } else {

                ChatUserListRoot.ChatUserItem chatUser = new Gson().fromJson(userJson, ChatUserListRoot.ChatUserItem.class);
                bindUserDetails(chatUser.getName(), chatUser.getImage(), chatUser.getAvatarFrameImage());
            }
        }

        // Start timer
        startTime = System.currentTimeMillis();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                binding.tvTimer.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);

//        initializePlayer(sourceUri);
        playAudio();
    }

    private void playAudio() {
        try {

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioAttributes(
                    new android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
            );

            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.fake_audio);
            mediaPlayer.setDataSource(this, uri);

            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> finish());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final SensorEventListener proximityListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float distance = event.values[0];

            if (distance < proximitySensor.getMaximumRange()) {
                // Phone near ear
                if (!wakeLock.isHeld()) wakeLock.acquire();


                Log.d(TAG, "EAR MODE → screen OFF + earpiece");
            } else {
                // Phone away
                if (wakeLock.isHeld()) wakeLock.release();

                Log.d(TAG, "NORMAL MODE → screen ON");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(proximityListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximitySensor != null) {
            sensorManager.registerListener(proximityListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

    }
}