package com.codder.ultimate.chat.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.chat.adapter.GiftEventAdapter;
import com.codder.ultimate.chat.modelclass.GiftEvent;
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

public class VideoCallActivity extends VideoCallBaseActivity {

    private static final String TAG = "VideoCallActivity";
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    ActivityVideoCallBinding binding;
    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;
    private FrameLayout mLocalContainer;
    private FrameLayout mRemoteContainer;
    private VideoCanvas mLocalVideo;
    private VideoCanvas mRemoteVideo;
    private boolean isVideoDecoded = false;
    double coin;

    private EmojiSheetViewModel giftViewModel;
    private EmojiBottomSheetFragment emojiBottomsheetFragment;

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

    private GiftEventAdapter giftFeedAdapter;
    private final java.util.ArrayList<GiftEvent> giftFeed = new java.util.ArrayList<>();
    private String guestUserName;
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

        private Queue<JSONObject> giftQueue = new LinkedList<>();
        private boolean isGiftDisplaying = false;

        @Override
        public void chatOrCallGiftSent(Object[] args) {
            runOnUiThread(() -> {
                try {
                    if (args == null || args.length == 0 || args[0] == null) return;

                    String raw = args[0].toString();
                    Log.d(TAG, "chatOrCallGiftSent payload: " + raw);

                    JSONObject root = new JSONObject(raw);
                    JSONObject data = root.optJSONObject("data");
                    if (data == null) return; // nothing useful

                    // 1) Enqueue for display if gift is present
                    if (data.has("gift")) {
                        giftQueue.add(data); // enqueue just the data object
//                        if (!isGiftDisplaying)
                            processNextGift();
                    }

                    // 2) Extract sender and gift info from *data*
                    String senderName = data.optString("userName", "");
                    String senderId = data.optString("senderId", "");
                    String giftStr = data.optString("gift", null);
                    int giftCount = data.optInt("giftCount", 1);
                    String senderAvatar = senderId.equals(sessionManager.getUser().getId())
                            ? sessionManager.getUser().getImage()
                            : remoteUserAvatar;

                    GiftRoot.GiftItem giftItem = null;
                    if (giftStr != null && giftStr.trim().startsWith("{")) {
                        giftItem = new Gson().fromJson(giftStr, GiftRoot.GiftItem.class);
                    } else {
                        JSONObject giftObj = data.optJSONObject("gift");
                        if (giftObj != null)
                            giftItem = new Gson().fromJson(giftObj.toString(), GiftRoot.GiftItem.class);
                    }
                    if (giftItem != null) giftItem.setCount(giftCount);

                    String giftAbsUrl = null;
                    if (giftItem != null) {
                        // prefer the canonical image we resolved earlier from the category list if you have it,
                        // otherwise fall back to the payload image
                        giftAbsUrl = ensureAbs(giftItem.getImage());
                    }

                    // finally: add row to the feed
                    addGiftToFeed(senderId, senderName, senderAvatar, giftAbsUrl, giftCount);

                    Log.d(TAG, "senderId=" + senderId + " localUser=" + sessionManager.getUser().getId());

                    // 3) Deduct diamonds only if local user sent the gift
                    if (sessionManager != null && sessionManager.getUser() != null
                            && giftItem != null && senderId.equals(sessionManager.getUser().getId())) {

                        double cost = giftItem.getCoin() * giftItem.getCount();
                        double current = sessionManager.getUser().getDiamond();
                        double newBalance = Math.max(0, current - cost);

                        UserRoot.User user = sessionManager.getUser();
                        user.setDiamond(newBalance);
                        sessionManager.saveUser(user);

                        if (giftViewModel != null) {
                            giftViewModel.localUserCoin.setValue(newBalance);
                        }

                        String balanceStr = String.format(Locale.US, "%.2f", newBalance);
                        Log.d(TAG, "Diamonds deducted, new balance ==> " + balanceStr);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "chatOrCallGiftSent error", e);
                }
            });
        }

        private void processNextGift() {
            if (giftQueue.isEmpty()) {
                isGiftDisplaying = false;
                return;
            }
            isGiftDisplaying = true;
            JSONObject giftJson = giftQueue.poll();
            if (giftJson == null) {
                processNextGift();
                return;
            }
            try {
                Object giftObj = giftJson.opt("gift");
                if (giftObj == null) {
                    processNextGift();
                    return;
                }
                GiftRoot.GiftItem giftData = new Gson().fromJson(giftObj.toString(), GiftRoot.GiftItem.class);
                if (giftData == null) {
                    processNextGift();
                    return;
                }
                String finalGiftLink = null;
                List<GiftRoot.GiftItem> giftItemList = sessionManager != null ? sessionManager.getGiftsList(giftData.getCategory()) : null;
                if (giftItemList != null) {
                    for (GiftRoot.GiftItem item : giftItemList) {
                        if (item != null && item.getId() != null && item.getId().equals(giftData.getId())) {
                            finalGiftLink = BuildConfig.BASE_URL + item.getImage();
                            break;
                        }
                    }
                }
                if (giftData.getType() == 2) {
                    displaySVGAAnimation(finalGiftLink, giftJson, giftData);
                } else if (giftData.getType() == 0 || giftData.getType() == 1) {
                    displayImageGift(finalGiftLink, giftJson, giftData);
                } else {
                    processNextGift();
                }
            } catch (Exception e) {
                e.printStackTrace();
                processNextGift();
            }
        }

        private void displayImageGift(String giftLink, JSONObject jsonObject, GiftRoot.GiftItem giftData) throws JSONException {
            if (isDestroyed() || isFinishing() || binding == null) {
                processNextGift();
                return;
            }
            Glide.with(VideoCallActivity.this).load(giftLink).into(binding.imgGift);
            Glide.with(VideoCallActivity.this).load(RayziUtils.getImageFromNumber(giftData.getCount())).into(binding.imgGiftCount);
            String name = jsonObject.optString("userName", "");
            binding.tvGiftUserName.setText(name + getString(R.string.sent_a_gift));
            binding.lytGift.setVisibility(VISIBLE);
            binding.tvGiftUserName.setVisibility(VISIBLE);

            new Handler().postDelayed(() -> {
                if (binding != null) {
                    binding.lytGift.setVisibility(GONE);
                    binding.tvGiftUserName.setVisibility(GONE);
                    binding.tvGiftUserName.setText("");
                    binding.imgGift.setImageDrawable(null);
                    binding.imgGiftCount.setImageDrawable(null);
                }
                processNextGift();
            }, 4000);
        }

        private void displaySVGAAnimation(String giftLink, JSONObject jsonObject, GiftRoot.GiftItem giftData) {
            if (isFinishing() || isDestroyed() || binding == null) {
                processNextGift();
                return;
            }

            // Hide static gift UI while SVGA plays
            binding.lytGift.setVisibility(GONE);
            binding.tvGiftUserName.setVisibility(GONE);

            final SVGAImageView imageView = binding.svgaImage;
            imageView.setVisibility(VISIBLE);

            // 1) Try cache first
            SvgaCacheManager.decodeSvgaFromCache(VideoCallActivity.this, giftLink, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    if (isFinishing() || isDestroyed() || binding == null || binding.getRoot().getWindowToken() == null) {
                        processNextGift();
                        return;
                    }

                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                    imageView.setImageDrawable(drawable);
                    imageView.startAnimation();

                    // Overlay: name + gift count
                    String name = jsonObject.optString("userName", "");
                    binding.tvSvgaGiftUserName.setText(name + " " + getString(R.string.sent_a_gift));
                    binding.lytSvgagift.setVisibility(VISIBLE);

                    Glide.with(binding.imgSvgaGiftCount)
                            .load(RayziUtils.getImageFromNumber(giftData.getCount()))
                            .into(binding.imgSvgaGiftCount);

                    long durationMs = (long) (svgaVideoEntity.getFrames() * 1000L / svgaVideoEntity.getFPS());
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (binding != null) {
                            binding.lytSvgagift.setVisibility(GONE);
                            imageView.clear();
                            imageView.setVisibility(GONE);
                        }
                        processNextGift();
                    }, durationMs);
                }

                @Override
                public void onError() {
                    // 2) Cache miss → fallback to network
                    try {
                        SVGAParser parser = new SVGAParser(VideoCallActivity.this);
                        parser.parse(new URL(giftLink), new SVGAParser.ParseCompletion() {
                            @Override
                            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                if (isFinishing() || isDestroyed() || binding == null || binding.getRoot().getWindowToken() == null) {
                                    processNextGift();
                                    return;
                                }

                                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                                imageView.setImageDrawable(drawable);
                                imageView.startAnimation();

                                String name = jsonObject.optString("userName", "");
                                binding.tvSvgaGiftUserName.setText(name + " " + getString(R.string.sent_a_gift));
                                binding.lytSvgagift.setVisibility(VISIBLE);

                                Glide.with(binding.imgSvgaGiftCount)
                                        .load(RayziUtils.getImageFromNumber(giftData.getCount()))
                                        .into(binding.imgSvgaGiftCount);

                                long durationMs = (long) (svgaVideoEntity.getFrames() * 1000L / svgaVideoEntity.getFPS());
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    if (binding != null) {
                                        binding.lytSvgagift.setVisibility(GONE);
                                        imageView.clear();
                                        imageView.setVisibility(GONE);
                                    }
                                    processNextGift();
                                }, durationMs);
                            }

                            @Override
                            public void onError() {
                                // Network also failed — skip this gift
                                imageView.setVisibility(GONE);
                                processNextGift();
                            }
                        });
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        imageView.setVisibility(GONE);
                        processNextGift();
                    }
                }
            });
        }

    };
    private String remoteUserAvatar;

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
                    jsonObject.put("callType",2);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_call);
        MySocketManager.getInstance().addCallHandler(callHandler);
        BaseActivity.STATUS_VIDEO_CALL = true;
        initView();

        setupGiftFeed();
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

                String uniqueId = user.getUniqueId();
                String displayName = "";

                if (uniqueId != null && !uniqueId.isEmpty()) {
                    if (!uniqueId.matches("\\d+")) {
                        displayName = "@" + uniqueId;
                    } else {
                        displayName = getString(R.string.id_) + uniqueId;
                    }
                }

                binding.tvUniqueId.setText(displayName);
                binding.imgProfile.setUserImage(user.getImage(), user.getAvatarFrameImage(), 30);
                remoteUserAvatar = user.getImage();
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
                    coin = sessionManager.getSetting().getMaleCallCharge();
                } else if (getIntent().getStringExtra("type").equalsIgnoreCase("Female")) {
                    coin = sessionManager.getSetting().getFemaleCallCharge();
                } else {
                    coin = 0;
                }
            }
        }

        if (otherUserId != null && !otherUserId.isEmpty()) {
            if (token != null && !token.isEmpty()) {
                initUI();
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
        giftViewModel = new ViewModelProvider(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);
        giftViewModel.initEmojiSheet(this);
        giftViewModel.getGiftCategory();
        emojiBottomsheetFragment = new EmojiBottomSheetFragment();
        binding.imggift2.setOnClickListener(v -> {
            if (!emojiBottomsheetFragment.isAdded())
                emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojiSheet");
        });

        giftViewModel.finalGift.observe(this, giftItem -> {
            if (giftItem != null) {
                double totalCoin = giftItem.getCoin() * giftItem.getCount();
                if (sessionManager.getUser().getDiamond() < totalCoin) {
                    Toast.makeText(VideoCallActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("senderId", sessionManager.getUser().getId());
                    jsonObject.put("receiverId", otherUserId);
                    jsonObject.put("giftId", giftItem.getId());
                    jsonObject.put("giftCount", giftItem.getCount());
                    jsonObject.put("eventType", Const.CAll);
                    jsonObject.put("userName", sessionManager.getUser().getName());
                    jsonObject.put("receiverUserName", guestUserName);
                    jsonObject.put("coin", giftItem.getCoin() * giftItem.getCount());
                    jsonObject.put("gift", new Gson().toJson(giftItem));

                    double totalGiftCoin = giftItem.getCoin();
                    double totalDiamond = sessionManager.getUser().getDiamond();

                    if (totalDiamond >= totalGiftCoin) {
                        MySocketManager.getInstance().getSocket().emit(Const.EVENT_CHAT_OR_CALL_GIFT_SENT, jsonObject);
                        Log.d(TAG, "chatOrCallGiftSent Emit called successfully. ===> " + jsonObject.toString());
                    } else {
                        Toast.makeText(VideoCallActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        binding.btnDecline.setOnClickListener(v -> onBackPressed());
        binding.localVideoViewContainer.setOnClickListener(v -> {
            switchView(mLocalVideo);
            switchView(mRemoteVideo);
        });
        binding.btnMute.setOnClickListener(v -> {
            mMuted = !mMuted;
            mRtcEngine.muteLocalAudioStream(mMuted);
            int res = mMuted ? R.drawable.ic_call_mute : R.drawable.ic_call_unmute;
            binding.btnMute.setImageResource(res);
        });
        binding.btnSwitchCamera.setOnClickListener(v -> mRtcEngine.switchCamera());
        binding.btnDecline.setOnClickListener(v -> {
            endCall();
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
        });
    }

    private void setupGiftFeed() {
        giftFeedAdapter = new GiftEventAdapter();
        binding.rvGiftFeed.setAdapter(giftFeedAdapter);
        binding.rvGiftFeed.setHasFixedSize(true);
        LinearLayoutManager giftLm = new LinearLayoutManager(this);
        giftLm.setStackFromEnd(true);
        giftLm.setReverseLayout(true);
        binding.rvGiftFeed.setLayoutManager(giftLm);

        // Avoid animation jumps that can fight with auto-scroll
        binding.rvGiftFeed.setItemAnimator(null);

        // Hard guarantee: whenever items are inserted, jump to the bottom
        giftFeedAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                binding.rvGiftFeed.post(() -> scrollGiftFeedToBottom(false));
            }
        });
    }

    private void setupRemoteVideo(int uid) {
        if (mRemoteVideo != null) return;

        TextureView view = new TextureView(getBaseContext());
        // Optional: if you plan to use alpha/transparency effects
        view.setOpaque(false);

        ViewGroup parent = mRemoteContainer;
        parent.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mRemoteVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        mRtcEngine.setupRemoteVideo(mRemoteVideo);
    }


    private void onRemoteUserLeft(int uid) {
        Log.d(TAG, "onRemoteUserLeft: ");
        if (mRemoteVideo != null && mRemoteVideo.uid == uid) {
            removeFromParent(mRemoteVideo);
            mRemoteVideo = null;
            endCall();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        endCall();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initUI() {
        mLocalContainer = binding.localVideoViewContainer;
        mRemoteContainer = binding.remoteVideoViewContainer;
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
        cleanupPreviousCall();
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
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
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15, VideoEncoderConfiguration.STANDARD_BITRATE, VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {
        TextureView view = new TextureView(getBaseContext());
        view.setOpaque(false); // optional
        mLocalContainer.addView(view);
        mLocalVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(mLocalVideo);
        mRtcEngine.startPreview();
    }


    private void joinChannel() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
        Log.d(TAG, "joinChannel: ");
        mRtcEngine.joinChannel(token, channel, 0, options);
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
            mRtcEngine.stopPreview();
            mRtcEngine.muteLocalVideoStream(true); // Disable the camera
        }

        removeFromParent(mLocalVideo);
        mLocalVideo = null;
        removeFromParent(mRemoteVideo);
        mRemoteVideo = null;
        leaveChannel();
        finish();
    }

    private ViewGroup removeFromParent(VideoCanvas canvas) {
        if (canvas != null) {
            ViewParent parent = canvas.view.getParent();
            if (parent != null) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(canvas.view);
                return group;
            }
        }
        return null;
    }

    private void switchView(VideoCanvas canvas) {
        ViewGroup parent = removeFromParent(canvas);
        if (parent == mLocalContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }
            mRemoteContainer.addView(canvas.view);
        } else if (parent == mRemoteContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }
            mLocalContainer.addView(canvas.view);
        }
    }

    private void cleanupPreviousCall() {
        if (mLocalVideo != null) {
            removeFromParent(mLocalVideo);
            mLocalVideo = null;
        }

        if (mRemoteVideo != null) {
            removeFromParent(mRemoteVideo);
            mRemoteVideo = null;
        }
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
        runOnUiThread(() -> new Handler().postDelayed(() -> {
            if (isVideoDecoded) {
                Log.d(TAG, "onJoinChannelSuccess: video decoded");
            } else {
                Toast.makeText(VideoCallActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
                endCall();
            }
        }, 5000));
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        runOnUiThread(() -> {
            onRemoteUserLeft(uid);
            endCall();
        });
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        runOnUiThread(() -> setupRemoteVideo(uid));
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

    private void addGiftToFeed(String senderId, String senderName, String senderAvatar, String giftAbsUrl, int count) {
        GiftEvent ev = new GiftEvent(senderId, senderName, senderAvatar, giftAbsUrl, count, System.currentTimeMillis());

        // keep a small, bounded list
        giftFeed.add(ev);
        if (giftFeed.size() > 50) giftFeed.remove(0);

        // submit a *new copy* for ListAdapter diff + force scroll to bottom
        giftFeedAdapter.submitList(new java.util.ArrayList<>(giftFeed), () ->
                binding.rvGiftFeed.post(() -> scrollGiftFeedToBottom(false)));
    }

    private static String ensureAbs(String maybeRelative) {
        if (maybeRelative == null) return null;
        if (maybeRelative.startsWith("http://") || maybeRelative.startsWith("https://"))
            return maybeRelative;
        return BuildConfig.BASE_URL + maybeRelative;
    }

    private void scrollGiftFeedToBottom(boolean smooth) {
        int last = Math.max(giftFeedAdapter.getItemCount() - 1, 0);
        if (smooth) binding.rvGiftFeed.smoothScrollToPosition(last);
        else binding.rvGiftFeed.scrollToPosition(last);
    }

}