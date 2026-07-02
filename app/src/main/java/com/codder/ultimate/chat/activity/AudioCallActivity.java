package com.codder.ultimate.chat.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.codder.ultimate.R;

import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.chat.adapter.GiftEventAdapter;
import com.codder.ultimate.chat.modelclass.GiftEvent;
import com.codder.ultimate.databinding.ActivityAudioCallBinding;
import com.codder.ultimate.databinding.ActivityVideoCallBinding;
import com.codder.ultimate.live.fragment.EmojiBottomSheetFragment;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.viewModel.EmojiSheetViewModel;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.socket.CallHandler;
import com.codder.ultimate.socket.MySocketManager;
import com.codder.ultimate.utils.SvgaCacheManager;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.google.gson.Gson;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class AudioCallActivity extends VideoCallBaseActivity {

    ActivityAudioCallBinding binding;
    private static final String TAG = "AudioCallActivity";

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;

    private boolean isSpeakerOn = false;
    private boolean isVideoDecoded = false;
    double coin;

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private PowerManager.WakeLock wakeLock;

    String otherUserId = "";
    private String token;
    private String channel;
    private boolean callByMe;
    private boolean random;
    private String callRoomId;
    Handler timerHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private int seconds = 0;
    Runnable timerRunnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            seconds++;
            if (callByMe && seconds % 60 == 0) {
                reduceCoin();
            }
            int p1 = seconds % 60;
            int p2 = seconds / 60;
            int p3 = p2 % 60;
            p2 = p2 / 60;

            String sec;
            String hour;
            String min;
            if (p1 < 10) {
                sec = "0" + p1;
            } else {
                sec = String.valueOf(p1);
            }
            if (p2 < 10) {
                hour = "0" + p2;
            } else {
                hour = String.valueOf(p2);
            }
            if (p3 < 10) {
                min = "0" + p3;
            } else {
                min = String.valueOf(p3);
            }
            binding.tvTimer.setText(hour + ":" + min + ":" + sec);

            timerHandler.postDelayed(this, 1000);
        }
    };

    private String guestUserName;

    private String remoteUserAvatar;
    CallHandler callHandler = new CallHandler() {

        @Override
        public void onCallRequest(Object[] args) {
            Log.d(TAG, "onCallRequest: " + Arrays.toString(args));
        }

        @Override
        public void onCallConfirm(Object[] args) {
            Log.d(TAG, "onCallConfirm: " + Arrays.toString(args));
        }

        @Override
        public void onCallAnswer(Object[] args) {
            Log.d(TAG, "onCallAnswer: " + Arrays.toString(args));
        }

        @Override
        public void onCallReceive(Object[] args) {
            runOnUiThread(() -> {
                if (args != null && args.length > 0 && args[0] != null) {
                    Log.d(TAG, "call: callReceive " + args[0].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        UserRoot.User user = new Gson().fromJson(jsonObject.toString(), UserRoot.User.class);
                        sessionManager.saveUser(user);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing call receive data: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onCallCancel(Object[] args) {
            // No logic for now
        }


        @Override
        public void chatOrCallGiftSent(Object[] args) {

        }


    };

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_audio_call);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        MySocketManager.getInstance().addCallHandler(callHandler);
        BaseActivity.STATUS_VIDEO_CALL = true;
        initView();

    }

    private void reduceCoin() {
        if (sessionManager.getUser().isIsVIP()) {
            if (sessionManager.getUser().getLevel().getAccessibleFunction().isFreeCall() && !sessionManager.getUser().isHost()) {
                Log.d(TAG, "reduceCoin: free call");
                return;
            }
        }

        UserRoot.User user = sessionManager.getUser();
        double usercoin = user.getDiamond();
        if (usercoin >= coin) {
            double finalCoin = usercoin - coin;
            if (finalCoin >= 0) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("callId", callRoomId);
                    jsonObject.put("coin", coin);
                    jsonObject.put("callType",1);
                    Log.d(TAG, "reduceCoin: callReceive event " + jsonObject);
                    Log.d(TAG, "reduceCoin: callReceive event " + coin);
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_RECEIVE, jsonObject);
                    user.setDiamond(finalCoin);
                    sessionManager.saveUser(user);
                } catch (JSONException e) {
                    Log.e(TAG, "Error reducing coins: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, getString(R.string.insufficient_coins), Toast.LENGTH_SHORT).show();
                endCall();
            }
        } else {
            Toast.makeText(this, getString(R.string.insufficient_coins), Toast.LENGTH_SHORT).show();
            endCall();
        }
    }

    private void initView() {

        Intent intent = getIntent();
        otherUserId = intent.getStringExtra(Const.USERID);
        token = intent.getStringExtra(Const.TOKEN);
        callRoomId = intent.getStringExtra(Const.CALL_ROOM_ID);
        channel = intent.getStringExtra(Const.CHANNEL);
        callByMe = intent.getBooleanExtra(Const.CALL_BY_ME, false);
        random = intent.getBooleanExtra("random", false);

        userApiCall.getGuestProfile(otherUserId, new UserApiCall.OnGuestUserApiListener() {
            @Override
            public void onUserGot(GuestProfileRoot.User user) {
                guestUserName = user.getName();
                binding.tvName.setText(guestUserName);

                binding.imgProfile.setUserImage(user.getImage(), user.getAvatarFrameImage(), 30);
                binding.imgMainProfile.setWithoutbgUserImage(user.getImage(), user.getAvatarFrameImage(), 0);
                remoteUserAvatar = user.getImage();

                Glide.with(AudioCallActivity.this)
                        .load(user.getImage())
                        .transform(new MultiTransformation<>(new BlurTransformation(50), new CenterCrop()))
                        .into(binding.backBlurImage);

            }

            @Override
            public void onFailure() {
            }
        });

        Log.d(TAG, "onCreate: random=" + random);
        Log.d(TAG, "onCreate: type=" + getIntent().getStringExtra("type"));

        if (random) {
            // Determine coin rate based on type
            if (getIntent().getStringExtra("type") != null) {
                if (getIntent().getStringExtra("type").equalsIgnoreCase("Male")) {
                    coin = sessionManager.getSetting().getMaleRandomCallRate();
                } else if (getIntent().getStringExtra("type").equalsIgnoreCase("Female")) {
                    coin = sessionManager.getSetting().getFemaleRandomCallRate();
                } else if (getIntent().getStringExtra("type").equalsIgnoreCase("Both")) {
                    coin = sessionManager.getSetting().getBothRandomCallRate();
                } else {
                    coin = 0;
                }
            }
        } else {
            // Determine coin rate based on type
            if (getIntent().getStringExtra("type") != null) {
                if (getIntent().getStringExtra("type").equalsIgnoreCase("Male")) {
                    coin = sessionManager.getSetting().getAudioCallChargeMale();
                } else if (getIntent().getStringExtra("type").equalsIgnoreCase("Female")) {
                    coin = sessionManager.getSetting().getAudioCallChargeFemale();
                } else {
                    coin = 0;
                }
            }
        }

        if (otherUserId != null && !otherUserId.isEmpty()) {
            if (token != null && !token.isEmpty()) {
                initListener();
                if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
                    initEngineAndJoinChannel();
                }

                timerHandler.postDelayed(timerRunnable, 1000);
                if (callByMe) {
                    reduceCoin();
                }
            }
        }
    }

    private void initListener() {


        binding.btnDecline.setOnClickListener(v -> onBackPressed());
        binding.btnMute.setOnClickListener(v -> {
            mMuted = !mMuted;
            mRtcEngine.muteLocalAudioStream(mMuted);
            int res = mMuted ? R.drawable.ic_call_mute : R.drawable.ic_call_unmute;
            binding.btnMute.setImageResource(res);
        });
        binding.btnDecline.setOnClickListener(v -> {
            endCall();
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
        });

        binding.ivSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            mRtcEngine.setEnableSpeakerphone(isSpeakerOn);
            if (isSpeakerOn) {
                mRtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
                mRtcEngine.setEnableSpeakerphone(true);
                binding.ivSpeaker.setImageResource(R.drawable.ic_call_speaker);
            } else {
                mRtcEngine.setDefaultAudioRoutetoSpeakerphone(false);
                mRtcEngine.setEnableSpeakerphone(false);
                binding.ivSpeaker.setImageResource(R.drawable.ic_call_speackeroff);
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        endCall();
    }

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


    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                showLongToast(getString(R.string.need_permissions) + Manifest.permission.RECORD_AUDIO + "/" + Manifest.permission.CAMERA);
                finish();
                return;
            }
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(String msg) {
        this.runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }

    private void initEngineAndJoinChannel() {
//        cleanupPreviousCall();
        initializeEngine();
        setupVideoConfig();
//        setupLocalVideo();
        joinChannel();
    }

    private void initializeEngine() {
        try {
            mRtcEngine = rtcEngine();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        mRtcEngine.disableVideo();
        mRtcEngine.enableAudio();
        mRtcEngine.setDefaultAudioRoutetoSpeakerphone(false);
        mRtcEngine.setEnableSpeakerphone(false);
    }



    private void joinChannel() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
        Log.d(TAG, "joinChannel: ");
        mRtcEngine.joinChannel(token, channel, 0, options);
        mRtcEngine.setEnableSpeakerphone(false);
    }

    @Override
    protected void onDestroy() {
        if (!mCallEnd) {
            leaveChannel();
        }
        BaseActivity.STATUS_VIDEO_CALL = false;
        Log.d(TAG, "reduseCoin: calldisconnect event ");
        MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_DISCONNECT, callRoomId);
        MySocketManager.getInstance().removeCallHandler(callHandler);

        timerHandler.removeCallbacks(timerRunnable);

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        super.onDestroy();
    }

    private void leaveChannel() {
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            mRtcEngine = null;
        }
    }

    private void endCall() {
        mCallEnd = true;

        if (mRtcEngine != null) {
            mRtcEngine.muteLocalVideoStream(true); // Disable the camera
        }
        leaveChannel();
        finish();
    }


    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        runOnUiThread(() -> {
            isVideoDecoded = true;

            Log.d(TAG, "sssss=- run: vide decode");
        });
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

        mRtcEngine.setEnableSpeakerphone(false);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        runOnUiThread(() -> {
            endCall();
        });
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {

    }

    @Override
    public void onLastmileQuality(int quality) {

    }

    @Override
    public void onErr(int err) {
        Log.d(TAG, "onErr: " + err);
    }

    @Override
    public void onConnectionLost() {

    }

    @Override
    public void onVideoStopped() {

    }

    @Override
    public void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result) {

    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {

    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {

    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {

    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {

    }

    @Override
    public void onChannelMediaRelayStateChanged(int state, int code) {

    }

    @Override
    public void onChannelMediaRelayEvent(int code) {

    }

    @Override
    public void onFirstLocalAudioFramePublished(int elapsed) {

    }

    @Override
    public void onFirstRemoteAudioFrame(int uid, int elapsed) {

    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {

    }

    @Override
    public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {

    }

    @Override
    public void onActiveSpeaker(int uid) {

    }

    @Override
    public void onAudioMixingStateChanged(int state, int reason) {

    }

    @Override
    public void onTokenPrivilegeWillExpire(String token) {

    }

    @Override
    public void onRequestToken() {

    }

    @Override
    public void onAudioRouteChanged(int routing) {

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


}