package com.codder.ultimate.chat.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.chat.modelclass.CallRequestRoot;
import com.codder.ultimate.databinding.ActivityCallRequestBinding;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.socket.CallHandler;
import com.codder.ultimate.socket.MySocketManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import io.agora.rtc2.IRtcEngineEventHandler;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallRequestActivity extends VideoCallBaseActivity {

    private static final String TAG = "CallRequestActivity";
    private ActivityCallRequestBinding binding;
    private GuestProfileRoot.User guestUser;
    private final Handler handler = new Handler();
    private boolean isGone = false;
    private String callRoomId;
    private String agoraToken;

    private final Runnable timeoutRunnable = () -> runOnUiThread(() -> {
        stopRingtone();
        Toast.makeText(this, guestUser.getName() + getString(R.string.is_busy_with_someone_else), Toast.LENGTH_SHORT).show();
        onBackPressed();
    });


    private final CallHandler callHandler = new CallHandler() {
        @Override
        public void onCallConfirm(Object[] args) {
            handleCallConfirm(args);
        }

        @Override
        public void onCallAnswer(Object[] args) {
            handleCallAnswer(args);
        }

        @Override
        public void onCallRequest(Object[] args) {
        }

        @Override
        public void onCallReceive(Object[] args) {
        }

        @Override
        public void onCallCancel(Object[] args) {
        }

        @Override
        public void chatOrCallGiftSent(Object[] args) {

        }
    };
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_request);
        BaseActivity.STATUS_VIDEO_CALL = true;

        MySocketManager.getInstance().addCallHandler(callHandler);

        if (parseIntent()) {
            initializeUI();

            makeCallRequest();
            handler.postDelayed(timeoutRunnable, 20000);
        }
    }

    private boolean parseIntent() {
        String userData = getIntent().getStringExtra(Const.USER);
        if (userData == null || userData.isEmpty()) return false;

        guestUser = new Gson().fromJson(userData, GuestProfileRoot.User.class);
        return guestUser != null;
    }

    private void initializeUI() {
        binding.tvName.setText(guestUser.getName());
        binding.imgUser.setWithoutbgUserImage(guestUser.getImage(), guestUser.getAvatarFrameImage(), 0);
        binding.btnDecline.setOnClickListener(v -> onBackPressed());

        Glide.with(this)
                .load(guestUser.getImage())
                .transform(new MultiTransformation<>(new BlurTransformation(50), new CenterCrop()))
                .into(binding.backBlurImage);
    }

    private void makeCallRequest() {
        if (sessionManager.getUser() == null || guestUser == null || guestUser.getUserId() == null) {
            Log.e(TAG, "makeCallRequest: Invalid user data.");
            finishWithError("Missing user information.");
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("callerUserId", sessionManager.getUser().getId());
        json.addProperty("receiverUserId", guestUser.getUserId());
        json.addProperty("channel", guestUser.getUserId());
        json.addProperty("callType", getIntent().getStringExtra("Calltype"));

        RetrofitBuilder.create().makeCallRequest(json).enqueue(new Callback<CallRequestRoot>() {
            @Override
            public void onResponse(Call<CallRequestRoot> call, Response<CallRequestRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CallRequestRoot result = response.body();

                    if (result.isStatus() && result.getCallId() != null && result.getToken() != null) {
                        callRoomId = result.getCallId();
                        agoraToken = result.getToken();
                        Log.d(TAG, "Call setup successful. Call ID: " + callRoomId);
                        emitCallRequest();
                    } else {
                        String message = result.getMessage() != null ? result.getMessage() : getString(R.string.call_request_rejected);

                        if (message.contains("blocked")) {
                            Log.w(TAG, "Call blocked due to user block status: " + message);
                            runOnUiThread(() -> {
                                Toast.makeText(CallRequestActivity.this, getString(R.string.sorry_this_call_can_t_be_completed_right_now), Toast.LENGTH_LONG).show();
                                finish();
                            });
                        } else {
                            Log.w(TAG, "Call setup failed: " + message);
                            finishWithError(message);
                        }

                    }
                } else {
                    Log.e(TAG, "Call request error. Code: " + response.code() + ", Message: " + response.message());
                    finishWithError("Call request failed. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<CallRequestRoot> call, Throwable t) {
                Log.e(TAG, "makeCallRequest: Network error", t);
                runOnUiThread(() -> {
                    Toast.makeText(CallRequestActivity.this, getString(R.string.call_setup_failed) + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void emitCallRequest() {
        try {
            JSONObject obj = new JSONObject();
            obj.put(Const.USERID1, guestUser.getUserId());
            obj.put(Const.USERID2, sessionManager.getUser().getId());
            obj.put(Const.USER2_NAME, sessionManager.getUser().getName());
            obj.put(Const.USER2_IMAGE, sessionManager.getUser().getImage());
            obj.put(Const.USER2_IMAGE_FRAME_IMAGE, sessionManager.getUser().getAvatarFrameImage());
            obj.put(Const.CALL_ROOM_ID, callRoomId);
            obj.put("Calltype", getIntent().getStringExtra("Calltype"));
            obj.put(Const.TOKEN, agoraToken);

            MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_REQUEST, obj);
            binding.tvStatus.setText(R.string.calling);
            startRingtone();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleCallConfirm(Object[] args) {
        runOnUiThread(() -> {
            if (args != null) {
                Log.d(TAG, "callConfirmLister: " + args[0].toString());
                try {
                    JSONObject jsonObject = new JSONObject(args[0].toString());
                    String userId1 = jsonObject.getString(Const.USERID1);
                    String userId2 = jsonObject.getString(Const.USERID2);
                    boolean isConfirm = jsonObject.getBoolean(Const.IS_CONFIRM);
                    if (userId1.equals(guestUser.getUserId())) {
                        if (userId2.equals(sessionManager.getUser().getId())) {
                            if (isConfirm) {
                                binding.tvStatus.setText(getString(R.string.ringing));
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleCallAnswer(Object[] args) {
        runOnUiThread(() -> {
            if (args != null) {
                Log.d(TAG, "callAnswerLister: " + args[0].toString());
                try {
                    JSONObject jsonObject = new JSONObject(args[0].toString());  // required field  token channel
                    String userId1 = jsonObject.getString(Const.USERID1);
                    String userId2 = jsonObject.getString(Const.USERID2);
                    String token = jsonObject.getString(Const.TOKEN);
                    String callRoomId = jsonObject.getString(Const.CALL_ROOM_ID);
                    String channel = jsonObject.getString(Const.CHANNEL);
                    Log.d(TAG, "guest id : " + guestUser.getUserId());
                    Log.d(TAG, "local  id : " + sessionManager.getUser().getId());
                    boolean isAccept = jsonObject.getBoolean(Const.IS_ACCEPT);
                    if (userId1.equals(guestUser.getUserId())) {
                        if (userId2.equals(sessionManager.getUser().getId())) {
                            if (isAccept) {
                                if (!isGone) {
                                    stopRingtone();
                                    isGone = true;
                                    Intent intent = null;
                                    if (getIntent().getStringExtra("Calltype").equals("video")){
                                         intent = new Intent(CallRequestActivity.this, VideoCallActivity.class);
                                    }else {
                                        intent = new Intent(CallRequestActivity.this, AudioCallActivity.class);
                                    }
                                    intent.putExtra(Const.USERID, userId1);
                                    intent.putExtra(Const.TOKEN, token);
                                    intent.putExtra(Const.CHANNEL, channel);
                                    intent.putExtra(Const.CALL_ROOM_ID, callRoomId);
                                    intent.putExtra(Const.CALL_BY_ME, true);
                                    intent.putExtra("type", getIntent().getStringExtra("type"));
                                    intent.putExtra("random", getIntent().getBooleanExtra("random", false));
                                    intent.putExtra(Const.USER, new Gson().toJson(guestUser));
                                    startActivity(intent);
                                }
                            } else {
                                stopRingtone();
                                Toast.makeText(CallRequestActivity.this, R.string.call_declined, Toast.LENGTH_SHORT).show();
                            }
                            finish();
                            BaseActivity.STATUS_VIDEO_CALL = true;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void finishWithError(String message) {
        runOnUiThread(() -> {
            stopRingtone();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        });
    }


    @Override
    public void onBackPressed() {
        stopRingtone();
        super.onBackPressed();
        emitCallCancel();
        BaseActivity.STATUS_VIDEO_CALL = false;
        finish();
    }

    private void emitCallCancel() {
        try {
            JSONObject obj = new JSONObject();
            obj.put(Const.USERID1, guestUser.getUserId());
            obj.put(Const.USERID2, sessionManager.getUser().getId());
            obj.put(Const.USER2_NAME, sessionManager.getUser().getName());
            obj.put(Const.USER2_IMAGE, sessionManager.getUser().getImage());
            obj.put(Const.CALL_ROOM_ID, callRoomId);
            obj.put("callId", callRoomId);

            if (getIntent().getStringExtra("Calltype").equals("video")){
                obj.put("callType",2);
            }else {
                obj.put("callType",1);
            }

            MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_CANCEL, obj);
            Log.d(TAG, "emitCallCancel: emit call cancel ..." + obj);
        } catch (JSONException e) {
            Log.e(TAG, "Error emitting call cancel", e);
        }
    }

    @Override
    protected void onDestroy() {
        stopRingtone();
        MySocketManager.getInstance().removeCallHandler(callHandler);
        handler.removeCallbacks(timeoutRunnable);
        super.onDestroy();
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


}
