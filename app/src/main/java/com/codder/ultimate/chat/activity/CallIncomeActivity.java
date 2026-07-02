package com.codder.ultimate.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityCallIncomeBinding;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.socket.CallHandler;
import com.codder.ultimate.socket.MySocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import io.agora.rtc2.IRtcEngineEventHandler;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class CallIncomeActivity extends VideoCallBaseActivity {

    private static final String TAG = "CallIncomeActivity";

    private ActivityCallIncomeBinding binding;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private String otherUserId , callType;
    private String callRoomId;
    private String agoraToken = "";

    private final Handler handler = new Handler();

    private final Runnable timeoutRunnable = () -> runOnUiThread(this::finish);

    private final CallHandler callHandler = new CallHandler() {
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
            Log.d(TAG, "onCallReceive: " + Arrays.toString(args));
        }

        @Override
        public void onCallCancel(Object[] args) {
            runOnUiThread(() -> {
                if (args != null) {
                    Log.d(TAG, "onCallCancel: " + args[0].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        String userId1 = jsonObject.getString(Const.USERID1);

                        if (userId1.equals(sessionManager.getUser().getId())) {
                            BaseActivity.STATUS_VIDEO_CALL = false;
                            onBackPressed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @Override
        public void chatOrCallGiftSent(Object[] args) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_income);

        MySocketManager.getInstance().addCallHandler(callHandler);
        BaseActivity.STATUS_VIDEO_CALL = true;

        processIntent(getIntent());
        setupListeners();
        startNotificationEffects();
        handler.postDelayed(timeoutRunnable, 20000);
    }

    private void processIntent(Intent intent) {
        if (intent == null) return;

        String dataStr = intent.getStringExtra(Const.DATA);
        boolean isAcceptClicked = intent.getBooleanExtra(Const.IS_ACCEPT_CLICK, false);

        if (dataStr != null && !dataStr.isEmpty()) {
            try {
                JSONObject json = new JSONObject(dataStr);
                otherUserId = json.optString(Const.USERID2, "");
                callRoomId = json.optString(Const.CALL_ROOM_ID, "");
                agoraToken = json.optString(Const.TOKEN, "");
                callType = json.optString("Calltype", "");

                String userName = json.optString(Const.USER2_NAME, "Unknown");
                String userImage = json.optString(Const.USER2_IMAGE, "");
                String frameImage = json.optString(Const.USER2_IMAGE_FRAME_IMAGE, "");

                binding.imgUser.setWithoutbgUserImage(userImage, frameImage, 0);
                binding.tvUserName.setText(userName);

                Glide.with(this)
                        .load(userImage)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new MultiTransformation<>(new BlurTransformation(95), new CenterCrop()))
                        .into(binding.ivPlaceholder);

                json.put(Const.IS_CONFIRM, true);
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_CONFIRMED, json);

                if (isAcceptClicked) {
                    binding.btnAccept.performClick();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing intent JSON", e);
            }
        }
    }

    private void setupListeners() {
        binding.btnAccept.setOnClickListener(v -> acceptCall());
        binding.btnDecline.setOnClickListener(v -> declineCall());
    }

    private void acceptCall() {
        try {
            JSONObject json = new JSONObject();
            json.put(Const.USERID1, sessionManager.getUser().getId());
            json.put(Const.USERID2, otherUserId);
            json.put(Const.TOKEN, agoraToken);
            json.put(Const.CALL_ROOM_ID, callRoomId);
            json.put(Const.CHANNEL, sessionManager.getUser().getId());
            json.put(Const.IS_ACCEPT, true);
            if (callType.equals("video")){
                json.put("callType",2);
            }else {
                json.put("callType",1);
            }

            MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_ANSWER, json);

            Intent intent = null;
            if (callType.equals("video")){
                intent = new Intent(this, VideoCallActivity.class);
            }else {
                intent = new Intent(this, AudioCallActivity.class);
            }
            intent.putExtra(Const.USERID, otherUserId);
            intent.putExtra(Const.TOKEN, agoraToken);
            intent.putExtra(Const.CALL_ROOM_ID, callRoomId);
            intent.putExtra(Const.CHANNEL, sessionManager.getUser().getId());
            intent.putExtra(Const.CALL_BY_ME, false);
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error accepting call", e);
        } finally {
            finish();
        }
    }

    private void declineCall() {
        try {
            JSONObject json = new JSONObject();
            json.put(Const.USERID1, sessionManager.getUser().getId());
            json.put(Const.USERID2, otherUserId);
            json.put(Const.TOKEN, agoraToken);
            json.put(Const.CALL_ROOM_ID, callRoomId);
            json.put(Const.CHANNEL, sessionManager.getUser().getId());
            json.put(Const.IS_ACCEPT, false);

            if (callType.equals("video")){
                json.put("callType",2);
            }else {
                json.put("callType",1);
            }

            MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_ANSWER, json);
        } catch (Exception e) {
            Log.e(TAG, "Error declining call", e);
        }

        BaseActivity.STATUS_VIDEO_CALL = false;
        onBackPressed();
    }

    private void startNotificationEffects() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] pattern = {0, 500, 300, 500};
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(1000);
            }
        }

        try {
            mediaPlayer = new MediaPlayer();
            AssetFileDescriptor afd = getAssets().openFd("call.mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "Error playing ringtone", e);
        }
    }

    @Override
    protected void onPause() {
        if (vibrator != null) {
            vibrator.cancel();
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MySocketManager.getInstance().removeCallHandler(callHandler);
        handler.removeCallbacks(timeoutRunnable);
    }


    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {

    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

    }

    @Override
    public void onUserOffline(int uid, int reason) {

    }

    @Override
    public void onUserJoined(int uid, int elapsed) {

    }

    @Override
    public void onLastmileQuality(int quality) {

    }

    @Override
    public void onErr(int err) {

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
}
