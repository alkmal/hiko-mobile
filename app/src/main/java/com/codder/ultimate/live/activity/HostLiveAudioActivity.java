package com.codder.ultimate.live.activity;

import static android.provider.MediaStore.MediaColumns.DATA;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.agora.AgoraBaseActivity;
import com.codder.ultimate.agora.RtcStatsView;
import com.codder.ultimate.agora.stats.RemoteStatsData;
import com.codder.ultimate.agora.stats.StatsData;
import com.codder.ultimate.agora.token.RtcTokenBuilderSample;
import com.codder.ultimate.bottomsheets.BottomSheetOptions;
import com.codder.ultimate.databinding.ActivityHostLiveAudioBinding;
import com.codder.ultimate.databinding.BottomSheetAudioroomSettingsBinding;
import com.codder.ultimate.databinding.BottomSheetOnlineProfileBinding;
import com.codder.ultimate.live.adapter.GiftReceiveAdapter;
import com.codder.ultimate.live.adapter.SeatAdapter;
import com.codder.ultimate.live.bottomsheet.BottomSheetAudioRoomChangePasscode;
import com.codder.ultimate.live.bottomsheet.BottomSheetAudioRoomName;
import com.codder.ultimate.live.bottomsheet.BottomSheetAudioRoomSetting;
import com.codder.ultimate.live.bottomsheet.BottomSheetAudioRoomWelcomeMsg;
import com.codder.ultimate.live.bottomsheet.BottomSheetAudioRoomWheatMode;
import com.codder.ultimate.live.bottomsheet.BottomSheetBannedList;
import com.codder.ultimate.live.bottomsheet.BottomSheetGameCasino;
import com.codder.ultimate.live.bottomsheet.BottomSheetGameList;
import com.codder.ultimate.live.bottomsheet.BottomSheetGameTeenPatti;
import com.codder.ultimate.live.bottomsheet.BottomSheetHostMic;
import com.codder.ultimate.live.bottomsheet.BottomSheetReactions;
import com.codder.ultimate.live.bottomsheet.BottomSheetViewersUserProfile;
import com.codder.ultimate.live.bottomsheet.BottomSheetViewersUsers;
import com.codder.ultimate.live.bottomsheet.DialogGame;
import com.codder.ultimate.live.bottomsheet.UserProfileBottomSheet;
import com.codder.ultimate.live.fragment.EmojiBottomSheetFragment;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.model.LiveStramComment;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.live.model.ReactionsViewModel;
import com.codder.ultimate.live.utils.FloatingButtonService;
import com.codder.ultimate.live.utils.HostAPICall;
import com.codder.ultimate.live.utils.UserSelectableClass;
import com.codder.ultimate.live.viewModel.EmojiSheetViewModel;
import com.codder.ultimate.live.viewModel.HostLiveViewModel;
import com.codder.ultimate.modelclass.BroadcastBannerRoot;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.musicfunction.AddMusicActivity;
import com.codder.ultimate.musicfunction.AudioDetails;
import com.codder.ultimate.musicfunction.AudioMixingController;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.socket.AudioRoomHandler;
import com.codder.ultimate.socket.MySocketManager;
import com.codder.ultimate.socket.SocketConnectHandler;
import com.codder.ultimate.utils.ImageUrlUtil;
import com.codder.ultimate.utils.SvgaCacheManager;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HostLiveAudioActivity extends AgoraBaseActivity {

    public static final String TAG = "HostLiveAudioActivity";
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    ActivityHostLiveAudioBinding binding;
    SessionManager sessionManager;
    JSONArray jsonArray;
    JSONArray finalArray;
    SeatAdapter seatAdapter;
    GridLayoutManager gridLayoutManager;
    JSONArray blockedUsersList = new JSONArray();
    UserProfileBottomSheet userProfileBottomSheet;
    long animationDurationMillis;
    EmojiBottomSheetFragment emojiBottomsheetFragment;
    List<PkAudioLiveUserRoot.UsersItem.SeatItem> bookedSeatItemList = new ArrayList<>();
    List<GiftRoot.GiftItem> giftList = new ArrayList<>();
    GiftReceiveAdapter giftReceiveAdapter;

    private final ReactionRunner hostReaction = new ReactionRunner();
    private final ReactionRunner seatReaction = new ReactionRunner();

    int uuid;
    int selfPosition = 0;

    public static int hostPosition = -1;
    private int userListPosition = 0;
    private HostLiveViewModel viewModel;
    private EmojiSheetViewModel giftViewModel;
    private PkAudioLiveUserRoot.UsersItem liveUser;
    int coin;
    HostAPICall hostAPICall;
    JSONArray blockUserList = new JSONArray();

    private RtcStatsView rtcStatsView;
    private BottomSheetReactions bottomSheetReactions;

    private Queue<JSONObject> giftQueue = new LinkedList<>();
    private boolean isGiftDisplaying = false;
    long timeStamp;
    private String lastLocalCommentText = "";
    private long lastLocalCommentAt = 0L;
    private boolean seatChangePending = false;
    private final Handler seatChangeHandler = new Handler(Looper.getMainLooper());
    private final Runnable clearSeatChangePending = () -> seatChangePending = false;

    //Music Function
    ArrayList<AudioDetails> confirmedSongs;

    private static final String SHARED_PREFS_NAME = "MyPrefs";
    private static final String KEY_CONFIRMED_SONGS = "confirmedSongs";

    private static final int REQUEST_ADD_MUSIC = 100;

    PopupBuilder popupBuilder;

    private boolean isOptionsExpanded = false;


    private void setupOptionsToggle() {
        List<View> iconViews = Arrays.asList(
                binding.btnReaction,
                binding.btnMute,
                binding.btnMuteAllSeats,
                binding.imgGame,
                binding.imgMusic
        );


        binding.imgOption.setOnClickListener(v -> {
            if (!isOptionsExpanded) {

                int delay = 0;
                for (int i = iconViews.size() - 1; i >= 0; i--) {
                    View icon = iconViews.get(i);
                    icon.setVisibility(View.VISIBLE);
                    icon.setAlpha(0f);
                    icon.setTranslationY(40f);
                    icon.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(200)
                            .setStartDelay(delay)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    delay += 50;
                }

            } else {
                // Collapse
                int delay = 0;
                for (View icon : iconViews) {
                    icon.animate()
                            .alpha(0f)
                            .translationY(30f)
                            .setDuration(150)
                            .setStartDelay(delay)
                            .setInterpolator(new AccelerateInterpolator())
                            .withEndAction(() -> icon.setVisibility(View.GONE))
                            .start();
                    delay += 40;
                }
            }
            isOptionsExpanded = !isOptionsExpanded;
        });


    }



        SocketConnectHandler socketConnectHandler = new SocketConnectHandler() {
        @Override
        public void onConnect() {
            emitHostRoomJoin();
        }

        @Override
        public void onDisconnect() {

        }

        @Override
        public void onReconnecting() {

        }

        @Override
        public void onReconnected(Object[] args) {
            Log.d(TAG, "onReconnected: " + (args != null && args.length > 0 ? args[0] : ""));
            emitHostRoomJoin();
            emitLiveRejoin();
        }
    };

    private void emitHostRoomJoin() {
        if (liveUser == null || sessionManager == null || sessionManager.getUser() == null || MySocketManager.getInstance().getSocket() == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("liveUserId", sessionManager.getUser().getId());
            jsonObject.put("userId", sessionManager.getUser().getId());
            jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
            jsonObject.put("liveHistoryId", liveUser.getLiveStreamingId());
            jsonObject.put("liveUserMongoId", liveUser.getId());
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_HOST_JOIN_AUDIO_ROOM, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "emitHostRoomJoin: ", e);
        }
    }

    private void emitLiveRejoin() {
        if (liveUser == null || sessionManager == null || sessionManager.getUser() == null || MySocketManager.getInstance().getSocket() == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
            jsonObject.put("liveHistoryId", liveUser.getLiveStreamingId());
            jsonObject.put("liveUserMongoId", liveUser.getId());
            jsonObject.put("userId", sessionManager.getUser().getId());
            MySocketManager.getInstance().getSocket().emit(Const.LIVE_REJOIN, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "emitLiveRejoin: ", e);
        }
    }

    private PkAudioLiveUserRoot.UsersItem.SeatItem viewerListItem = null;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    handleImageSelection(uri);
                } else {
                    Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
                }
            });


    /** Copy picked image to cache, preview it, and upload as room image (handles mime + errors)*/
    private void handleImageSelection(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        // Preview
        Glide.with(this).load(uri).into(binding.imgProfile);

        String mime = getContentResolver().getType(uri);
        if (mime == null) mime = "image/*";

        File cacheFile = new File(getCacheDir(), "room_" + System.currentTimeMillis() + ".img");

        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(cacheFile)) {

            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            out.flush();

            RequestBody requestFile = RequestBody.create(MediaType.parse(mime), cacheFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("roomImage", cacheFile.getName(), requestFile);

            RequestBody liveUserIdBody = RequestBody.create(MediaType.parse("text/plain"), liveUser.getLiveUserId());
            HashMap<String, RequestBody> map = new HashMap<>();
            map.put("liveUserId", liveUserIdBody);

            RetrofitBuilder.create().updateRoomImage(map, body).enqueue(new Callback<RestResponse>() {
                @Override
                public void onResponse(@NonNull Call<RestResponse> call, @NonNull Response<RestResponse> resp) {
                    if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                        Toast.makeText(HostLiveAudioActivity.this, getString(R.string.room_image_updated), Toast.LENGTH_SHORT).show();
                        if (resp.body().getRoomImage() != null && !resp.body().getRoomImage().isEmpty()) {
                            liveUser.setRoomImage(resp.body().getRoomImage());
                            Glide.with(HostLiveAudioActivity.this).load(ImageUrlUtil.normalize(resp.body().getRoomImage())).into(binding.imgProfile);
                        }
                        Log.d(TAG, "Room image updated successfully.");
                    } else {
                        Log.w(TAG, "Failed to update room image: " +
                                (resp.body() != null ? resp.body().getMessage() : "No message"));
                        Toast.makeText(HostLiveAudioActivity.this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RestResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error updating room image", t);
                    Toast.makeText(HostLiveAudioActivity.this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Selected image not found", e);
            Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Failed to process selected image", e);
            Toast.makeText(this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
        }
    }

    /** Socket callback hub: queues toast/banner/game notifications and animates them one-by-one */
    AudioRoomHandler audioRoomHandler = new AudioRoomHandler() {

        @Override
        public void onBroadcastNotification(Object[] args) {
            Log.d(TAG, "onBroadcastNotification: ======" + args[0].toString());
            handleNotification(args, "onBrodcastNotification");
        }

        @Override
        public void onRoomWelcome(Object[] args) {
            runOnUiThread(() -> {
                if (args == null || args.length == 0 || args[0] == null) return;

                try {
                    org.json.JSONObject data = new org.json.JSONObject(args[0].toString());
                    String liveId = data.optString("liveStreamingId", "");
                    String msg = data.optString("roomWelcome", "");

                    if (liveUser != null && liveId.equals(liveUser.getLiveStreamingId())) {
                        liveUser.setRoomWelcome(msg);

                        if (viewModel != null && viewModel.liveStramCommentAdapter != null) {
                            String prefix = getString(R.string.announcement);
                            String text = (msg != null && !msg.isEmpty())
                                    ? prefix + msg
                                    : getString(R.string.announcement_welcome_to_room);

                            // ✅ Update the existing announcement row (no new comment)
                            viewModel.liveStramCommentAdapter.updateFirstAnnouncement(prefix, text);
                        }
                    }
                } catch (org.json.JSONException e) {
                    android.util.Log.e(TAG, "roomWelcome: invalid payload", e);
                }
            });
        }

        @Override
        public void onGame(Object[] args) {
            handleNotification(args, "ongame");
        }

        private Queue<JSONObject> notificationQueue = new LinkedList<>();
        private boolean isAnimating = false;

        private void handleNotification(Object[] args, String eventType) {
            runOnUiThread(() -> {
                try {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    JSONObject jsonObject = new JSONObject(args[0].toString());
                    enqueueNotification(jsonObject, eventType);

                } catch (JSONException e) {
                    Log.e(TAG, "handleNotification: ", e);
                }
            });
        }


        private void enqueueNotification(JSONObject jsonObject, String eventType) {
            try {
                // Add the event type for distinguishing between onGame and onBroadcastNotification
                jsonObject.put("eventType", eventType);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            notificationQueue.offer(jsonObject);
            if (!isAnimating) {
                showNextNotification();
            }
        }

        /**
         * Processes the next notification in the queue.
         */
        private void showNextNotification() {
            if (notificationQueue.isEmpty()) {
                isAnimating = false;
                return;
            }

            isAnimating = true;
            JSONObject jsonObject = notificationQueue.poll();

            try {
                String eventType = jsonObject.getString("eventType");
                String name;
                String imageUrl;
                String giftUrl;

                if ("ongame".equals(eventType)) {
                    // Handle onGame JSON structure
                    name = jsonObject.getString("message");
                    imageUrl = jsonObject.getString("userImage");
                    binding.icGiftImage.setVisibility(GONE);
                    // Load user image
                    Glide.with(HostLiveAudioActivity.this)
                            .load(imageUrl)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .into(binding.ivGameUserImage);

                    binding.tvGameNotification.setText(name);

                    // Load random banner image if available
                    List<BroadcastBannerRoot.BroadcastBannerItem> bannerItemList = sessionManager.getGameBroadcastBannerList();
                    if (bannerItemList != null && !bannerItemList.isEmpty()) {
                        String randomBannerUrl = getRandomBannerImage(bannerItemList);

                        Glide.with(HostLiveAudioActivity.this)
                                .load(BuildConfig.BASE_URL + randomBannerUrl)
                                .into(new CustomTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        binding.lytGameNotification.setBackground(resource);
                                        startGameBannerAnimation();
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        // Optionally handle the placeholder if needed
                                    }
                                });
                    } else {
                        startGameBannerAnimation();
                    }

                } else if ("onBrodcastNotification".equals(eventType)) {
                    // Handle onBroadcastNotification JSON structure
                    name = jsonObject.getString("message");
                    JSONObject sendData = new JSONObject(jsonObject.getJSONObject("sendData").toString());
                    imageUrl = sendData.getString("senderUserImage");
                    giftUrl = BuildConfig.BASE_URL + sendData.getString("giftImage");
                    binding.icGiftImage.setVisibility(VISIBLE);
                    // Load user image
                    Glide.with(HostLiveAudioActivity.this)
                            .load(imageUrl)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .into(binding.ivUserImage);

                    binding.tvNotification.setText(name);
                    Glide.with(HostLiveAudioActivity.this)
                            .load(giftUrl)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .into(binding.icGiftImage);

                    Log.d(TAG, "showNextNotification: ========" + name);

                    // Load random banner image if available
                    List<BroadcastBannerRoot.BroadcastBannerItem> bannerItemList = sessionManager.getBroadcastBannerList();
                    if (bannerItemList != null && !bannerItemList.isEmpty()) {
                        String randomBannerUrl = getRandomBannerImage(bannerItemList);

                        Glide.with(HostLiveAudioActivity.this)
                                .load(BuildConfig.BASE_URL + randomBannerUrl)
                                .into(new CustomTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        binding.lytNotification.setBackground(resource);
                                        startBannerAnimation();
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        // Optionally handle the placeholder if needed
                                    }
                                });
                    } else {
                        startBannerAnimation();
                    }

                } else {
                    return; // Unknown event type
                }


            } catch (JSONException e) {
                Log.e(TAG, "showNextNotification: ", e);
            }
        }

        private void startBannerAnimation() {
            binding.lytNotification.setVisibility(View.VISIBLE);

            Animation slideIn = AnimationUtils.loadAnimation(HostLiveAudioActivity.this, R.anim.anim_slide_right_to_left);
            binding.lytNotification.startAnimation(slideIn);

            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Animation slideOut = AnimationUtils.loadAnimation(HostLiveAudioActivity.this, R.anim.slide_right_to_left);
                        binding.lytNotification.startAnimation(slideOut);

                        slideOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                binding.lytNotification.setVisibility(GONE);
                                isAnimating = false;
                                showNextNotification(); // Process the next notification
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                    }, 3000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        private void startGameBannerAnimation() {
            binding.lytGameNotification.setVisibility(VISIBLE);

            Animation slideIn = AnimationUtils.loadAnimation(HostLiveAudioActivity.this, R.anim.anim_slide_right_to_left);
            binding.lytGameNotification.startAnimation(slideIn);

            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Animation slideOut = AnimationUtils.loadAnimation(HostLiveAudioActivity.this, R.anim.slide_right_to_left);
                        binding.lytGameNotification.startAnimation(slideOut);

                        slideOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                binding.lytGameNotification.setVisibility(GONE);
                                isAnimating = false;
                                showNextNotification(); // Process the next notification
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                    }, 3000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        private String getRandomBannerImage(List<BroadcastBannerRoot.BroadcastBannerItem> bannerItemList) {
            if (bannerItemList == null || bannerItemList.isEmpty()) {
                return "";
            }

            int randomIndex = new Random().nextInt(bannerItemList.size());
            return bannerItemList.get(randomIndex).getImageUrl();
        }


        @Override
        public void onTotalRoomCoins(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args[0] != null) {
                        Log.d(TAG, "onTotalRoomCoins: ==> " + args[0].toString());
                        binding.tvRcoins.setText(args[0].toString());
                    }
                }
            });
        }

        @Override
        public void onAudioLiveHostRemove(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args[0] != null) {
                        Log.d(TAG, "run: onAudioLiveHostRemove ==> " + args[0].toString());
                    }
                }
            });
        }

        @Override
        public void onHostEnter(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args[0] != null) {
                        Log.d(TAG, "onHostEnter: ==>" + args[0].toString());
                    }

                }
            });
        }

        @Override
        public void onLiveEndByEnd(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {
                    Log.d(TAG, "onLiveEndByEnd: ==> " + args[0].toString());
                    removeRtcVideo(0, true);

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("liveRoom", liveUser.getLiveStreamingId());
                        jsonObject.put("liveHostRoom", sessionManager.getUser().getId());
                    } catch (JSONException e) {
                        Log.e(TAG, "onLiveEndByEnd: ", e);
                    }

                    MySocketManager.getInstance().getSocket().emit("liveHostEnd", jsonObject);
                    PopupBuilder popupBuilder = new PopupBuilder(HostLiveAudioActivity.this);
                    popupBuilder.showLiveEndPopup(getString(R.string.your_live_session_end_by_admin_text), getString(R.string.dismiss), () -> {

                        startActivity(new Intent(HostLiveAudioActivity.this, LiveSummaryActivity.class).putExtra(Const.DATA, liveUser.getLiveStreamingId()));
                        finish();
                        Toast.makeText(HostLiveAudioActivity.this, getString(R.string.end_live_video), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "liveEndByEnd: liveEndByEnd" + args[0].toString());

                    });

                });
            }

        }

        @Override
        public void onUserCoinUpdate(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {
                    Log.d(TAG, "onUserCoinUpdate: ==> " + args[0].toString());
                    UserRoot.User user = sessionManager.getUser();
                    user.setDiamond(Integer.parseInt(args[0].toString()));
                    sessionManager.saveUser(user);
                });
            }
        }

        @Override
        public void onComment(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {
                    String data = args[0].toString();
                    Log.d(TAG, "onComment: ==> " + data);
                    if (!data.isEmpty()) {
                        LiveStramComment liveStramComment;
                        try {
                            liveStramComment = new Gson().fromJson(data, LiveStramComment.class);
                        } catch (RuntimeException error) {
                            Log.w(TAG, "Ignoring malformed realtime comment", error);
                            return;
                        }
                        if (liveStramComment != null) {
                            if (isDuplicateLocalComment(liveStramComment)) {
                                return;
                            }
                            viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
                            scrollAdapterLogic();
                        }
                    }
                });
            }
        }

        @Override
        public void onRoomHistory(Object[] args) {
            Log.d(TAG, "Ignoring roomHistory; audio comments are live-only for this session.");
        }


        @Override
        public void onGift(Object[] args) {

            runOnUiThread(() -> {
                if (args.length > 0 && args[0] != null) {
                    String data = args[0].toString();
                    Log.d(TAG, "onGift: 0 ==> " + args[0]);
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        if (jsonObject.get("gift") != null) {
                            // Add the gift data to the queue
                            giftQueue.add(jsonObject);
                            // Start processing if not already running
                            if (!isGiftDisplaying) {
                                processNextGift();
                            }
                        }

                        coin = jsonObject.getInt("coin");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Handle sender details (args[1])
                if (args.length > 1 && args[1] != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(args[1].toString());
                        Log.d(TAG, "onGift 1: ==> " + jsonObject.toString());
                        UserRoot.User user = new Gson().fromJson(jsonObject.toString(), UserRoot.User.class);
                        if (user != null && user.getId().equals(sessionManager.getUser().getId())) {
                            sessionManager.saveUser(user);
                            giftViewModel.localUserCoin.setValue(user.getDiamond());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Handle host details (args[2])
                if (args.length > 2 && args[2] != null) {
                    try {
                        Log.d(TAG, "onGift2: ==> " + args[2].toString());
                        JSONObject jsonObject = new JSONObject(args[2].toString());
                        UserRoot.User host = new Gson().fromJson(jsonObject.toString(), UserRoot.User.class);
                        if (host != null && host.getId().equals(sessionManager.getUser().getId())) {
//                            updateCoin = updateCoin + coin;
//                            sessionManager.saveInt("updateCoin",updateCoin);
//                            binding.tvRcoins.setText(String.valueOf(host.getRCoin()));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }


        // Gift pipeline head: serialize gifts, dedupe by timestamp, render SVGA/WEBP, chain next
        private void processNextGift() {
            if (!giftQueue.isEmpty()) {
                isGiftDisplaying = true;
                JSONObject giftJson = giftQueue.poll(); // Get the next gift
                try {

                    long receivedTimeStamp = giftJson.getLong("timeStamp");

                    if (timeStamp != receivedTimeStamp) {
                        timeStamp = receivedTimeStamp;

                        GiftRoot.GiftItem giftData = new Gson().fromJson(giftJson.get("gift").toString(), GiftRoot.GiftItem.class);

                        Log.d(TAG, "processNextGift: =================>>>>> " + giftData);
                        if (giftData != null) {
                            String finalGiftLink = null;
                            List<GiftRoot.GiftItem> giftItemList = sessionManager.getGiftsList(giftData.getCategory());
                            if (giftItemList != null) {
                                for (GiftRoot.GiftItem item : giftItemList) {
                                    if (giftData.getId().equals(item.getId())) {
                                        finalGiftLink = BuildConfig.BASE_URL + item.getImage();
                                        break;
                                    }
                                }
                            }
                            if (finalGiftLink == null || finalGiftLink.isEmpty()) {
                                String image = giftData.getType() == 2 && giftData.getSvgaImage() != null && !giftData.getSvgaImage().isEmpty()
                                        ? giftData.getSvgaImage()
                                        : giftData.getImage();
                                finalGiftLink = image != null && image.startsWith("http") ? image : BuildConfig.BASE_URL + (image == null ? "" : image);
                            }
                            String name = giftJson.getString("userName");
                            giftData.setName(name);
                            String receiverName = giftJson.getString("receiverUserName");
                            giftData.setReceiverUserName(Collections.singletonList(receiverName));

                            if (giftData.getType() == 2) {
                                if (finalGiftLink.contains(".webp")) {
                                    handleImageGift(finalGiftLink, giftData);
                                } else {
                                    handleSVGAGift(finalGiftLink, giftJson, giftData);
                                }
                            } else {
                                handleImageGift(finalGiftLink, giftData);
                            }

                            if (liveUser.getId().equals(sessionManager.getUser().getId())) {
                                // This user is the host receiving the gift
                                double currentCoins = Double.parseDouble(binding.tvRcoins.getText().toString());
                                double newGiftCoins = giftData.getCoin() * giftData.getCount();
                                double updatedCoins = currentCoins + newGiftCoins;
                                binding.tvRcoins.setText(RayziUtils.formatCoin(updatedCoins));
                                Log.d(TAG, "[RCoin] Host Gift Received - Coins Added: =================>>>>> " + newGiftCoins + ", Updated Total: " + updatedCoins);

                                Log.d(TAG, "Gift received by host: Added coins = =================>>>>> " + newGiftCoins + " => New total: " + updatedCoins);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                isGiftDisplaying = false;
                if (!giftQueue.isEmpty()) {
                    processNextGift();
                }

            } else {
                isGiftDisplaying = false;
            }
        }

        // Render static/gif gift in mini strip and auto-remove after 3s
        private void handleImageGift(String giftLink, GiftRoot.GiftItem giftData) {
            giftList.clear();
            giftList.add(giftData);
            giftReceiveAdapter.addData(giftList);

            new Handler().postDelayed(() -> {
                giftReceiveAdapter.remove(giftData);
                giftList.remove(giftData);
                processNextGift(); // Start the next gift immediately
            }, 3000); // Use the same duration as in your original code
        }

        // Render SVGA gift (cache → URL fallback), compute duration, then chain next
        private void handleSVGAGift(String giftLink, JSONObject jsonObject, GiftRoot.GiftItem giftData) {
            binding.lytGift.setVisibility(GONE);
            binding.tvGiftUserName.setVisibility(GONE);
            binding.svgaImage.setVisibility(View.VISIBLE);
            SVGAImageView imageView = binding.svgaImage;

            SvgaCacheManager.decodeSvgaFromCache(HostLiveAudioActivity.this, giftLink, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                    Log.d(TAG, "✅ Loaded SVGA from cache or fallback: " + giftLink);

                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                    imageView.setImageDrawable(drawable);
                    imageView.startAnimation();

                    binding.lytSvgagift.setVisibility(View.VISIBLE);
                    binding.tvSvgaGiftUserName.setText(giftData.getName() + getString(R.string.sent_a_gift_to) + giftData.getReceiverUserName());
                    Glide.with(binding.imgSvgaGiftCount)
                            .load(RayziUtils.getImageFromNumber(giftData.getCount()))
                            .into(binding.imgSvgaGiftCount);

                    long duration = svgaVideoEntity.getFrames() * 1000L / svgaVideoEntity.getFPS();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        binding.lytSvgagift.setVisibility(GONE);
                        imageView.clear();
                        imageView.setVisibility(GONE);
                        processNextGift();
                    }, duration);
                }

                @Override
                public void onError() {
                    Log.w(TAG, "⚠️ SVGA not found in cache, falling back to URL: " + giftLink);
                    try {
                        SVGAParser parser = new SVGAParser(HostLiveAudioActivity.this);
                        parser.decodeFromURL(new URL(giftLink), new SVGAParser.ParseCompletion() {
                            @Override
                            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                                imageView.setImageDrawable(drawable);
                                imageView.startAnimation();

                                binding.lytSvgagift.setVisibility(View.VISIBLE);
                                binding.tvSvgaGiftUserName.setText(giftData.getName() + getString(R.string.sent_a_gift_to) + giftData.getReceiverUserName());
                                Glide.with(binding.imgSvgaGiftCount)
                                        .load(RayziUtils.getImageFromNumber(giftData.getCount()))
                                        .into(binding.imgSvgaGiftCount);

                                long duration = svgaVideoEntity.getFrames() * 1000L / svgaVideoEntity.getFPS();

                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    binding.lytSvgagift.setVisibility(GONE);
                                    imageView.clear();
                                    imageView.setVisibility(GONE);
                                    processNextGift();
                                }, duration);
                            }

                            @Override
                            public void onError() {
                                Log.e(TAG, "❌ SVGA failed to load from URL: " + giftLink);
                                processNextGift();
                            }
                        }, null);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        processNextGift();
                    }
                }
            });
        }

        private Queue<JSONObject> entryQueue = new LinkedList<>();
        private boolean isEntryEffectRunning = false;


        @Override
        public void onView(Object[] args) {
            HostLiveAudioActivity.this.runOnUiThread(() -> {
                Log.d(TAG, "onView: viewListener " + args.toString());

                if (args[0] != null) {
                    try {
                        Log.d(TAG, "onView: 0 ==> " + args[0].toString());
                        jsonArray = new JSONArray(args[0].toString());
                        finalArray = new JSONArray();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject.getBoolean("isAdd")) {
                                finalArray.put(jsonObject);
                            }
                        }
                        if (viewModel != null && viewModel.liveViewUserAdapter != null) {
                            List<JSONObject> userList = new ArrayList<>();
                            for (int i = 0; i < finalArray.length(); i++) {
                                try {
                                    userList.add(finalArray.getJSONObject(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            viewModel.liveViewUserAdapter.submitList(userList);

                        }
                        binding.tvViewUserCount.setText(String.valueOf(finalArray.length()));
                        Log.d(TAG, "views2 : " + jsonArray);
                        binding.tvNoOneJoined.setVisibility(finalArray.length() > 0 ? View.GONE : View.VISIBLE);

                    } catch (JSONException e) {
                        Log.d(TAG, "207: ");
                        e.printStackTrace();
                    }
                }

                if (args.length > 1 && args[1] != null) {
                    try {
                        Log.d(TAG, "onView: 1 ==> " + args[1].toString());
                        JSONObject jsonObject = new JSONObject(args[1].toString());
                        if (jsonObject.has("entrySvga") && jsonObject.has("avatarFrame") && jsonObject.has("image")) {
                            Log.d(TAG, "onView: New Entry Detected: " + jsonObject.toString());
                            entryQueue.add(jsonObject);
                            triggerNextEntryEffect();  // Trigger the next effect
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });

        }

        private void triggerNextEntryEffect() {
            if (isEntryEffectRunning || entryQueue.isEmpty() || isFinishing()) return;

            isEntryEffectRunning = true;
            JSONObject jsonObject = entryQueue.poll();  // Get next user entry

            try {
                String avatarFrame = jsonObject.getString("avatarFrame");
                String entrySvga = jsonObject.getString("entrySvga");
                String userImage = jsonObject.getString("image");
                String userName = jsonObject.getString("userName");
                boolean isUserBackgroundLive = jsonObject.getBoolean("isUserBackgroundLive");

                if (isUserBackgroundLive) {
                    isEntryEffectRunning = false;
                    triggerNextEntryEffect();
                    return;
                }

                displaySVGAEntry(entrySvga);

                // Update UI for user details
                if (!entrySvga.isEmpty()) {
                    binding.layEntry.setVisibility(VISIBLE);
                    binding.userName.setText(userName);
                    Glide.with(HostLiveAudioActivity.this).load(ImageUrlUtil.normalize(userImage)).circleCrop().into(binding.userImage);
                    Glide.with(HostLiveAudioActivity.this).load(avatarFrame != null && !avatarFrame.isEmpty() ? ImageUrlUtil.normalize(avatarFrame) : "").into(binding.avatarFrameImage);

                    Animation animation = AnimationUtils.loadAnimation(HostLiveAudioActivity.this, R.anim.slide_in_right);
                    animation.setFillAfter(true);
                    binding.nameLyt.startAnimation(animation);
                } else {
                    binding.layEntry.setVisibility(GONE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                isEntryEffectRunning = false;
                binding.layEntry.setVisibility(View.GONE);
                triggerNextEntryEffect();  // Proceed even if something fails
            }
        }

        private void displaySVGAEntry(String entryLink) {

            binding.svgImage.clear();
            binding.layEntry.setVisibility(View.VISIBLE);
            SVGAImageView imageView = binding.svgImage;

            SvgaCacheManager.decodeSvgaFromCache(HostLiveAudioActivity.this, BuildConfig.BASE_URL + entryLink, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {

                    Log.d(TAG, "✅ Loaded SVGA from cache or fallback: " + entryLink);

                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                    imageView.setImageDrawable(drawable);
                    imageView.startAnimation();

                    binding.layEntry.setVisibility(View.VISIBLE);
                    binding.svgImage.setVisibility(View.VISIBLE);


                    long duration = svgaVideoEntity.getFrames() * 1000L / svgaVideoEntity.getFPS();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        binding.layEntry.setVisibility(GONE);
                        binding.svgImage.setVisibility(GONE);
                        imageView.clear();
                        imageView.setVisibility(GONE);
                    }, duration);

                }

                @Override
                public void onError() {

                    SVGAParser parser = new SVGAParser(HostLiveAudioActivity.this);
                    try {
                        parser.decodeFromURL(new URL(entryLink != null && !entryLink.isEmpty() ? BuildConfig.BASE_URL + entryLink : ""), new SVGAParser.ParseCompletion() {
                            @Override
                            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
                                dynamicEntity.setDynamicImage(BuildConfig.BASE_URL + entryLink, "99");
                                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity, dynamicEntity);
                                Log.d(TAG, "onComplete: from server==========" + BuildConfig.BASE_URL + entryLink);
                                imageView.setImageDrawable(drawable);
                                imageView.startAnimation();

                                animationDurationMillis = svgaVideoEntity.getFrames() / svgaVideoEntity.getFPS() * 1000L;

                                new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                                    binding.svgImage.setVisibility(GONE);
                                    binding.layEntry.setVisibility(GONE);
                                    binding.svgImage.clear();
                                }, animationDurationMillis);
                            }

                            @Override
                            public void onError() {

                            }
                        }, null);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onAddRequested(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args[0] != null) {
                        Log.d(TAG, "run: onAddRequested ==> " + args[0].toString());
                        try {
                            JSONObject request = new JSONObject(args[0].toString());
                            if (!request.optBoolean("request", false)) {
                                return;
                            }
                            if (liveUser == null || !request.optString("liveStreamingId").equals(liveUser.getLiveStreamingId())) {
                                return;
                            }
                            String requesterId = request.optString("userId");
                            if (sessionManager.getUser() != null && requesterId.equals(sessionManager.getUser().getId())) {
                                return;
                            }

                            String name = request.optString("name", "User");
                            String image = request.optString("image", "");
                            String avatarFrame = request.optString("avatarFrame", request.optString("avatarFrameImage", ""));
                            new PopupBuilder(HostLiveAudioActivity.this).showPkRequestPopup(
                                    name + " wants to take the mic",
                                    image,
                                    avatarFrame,
                                    "Accept",
                                    "Decline",
                                    new PopupBuilder.OnMultiButtonPopupLister() {
                                        @Override
                                        public void onClickContinue() {
                                            try {
                                                request.put("request", false);
                                                request.put("mute", request.optInt("mute", 0));
                                                MySocketManager.getInstance().getSocket().emit(Const.EVENT_ADD_REQUESTED, request.toString());
                                            } catch (JSONException e) {
                                                Log.e(TAG, "onAddRequested accept: ", e);
                                            }
                                        }

                                        @Override
                                        public void onClickCancel() {
                                        }
                                    });
                        } catch (JSONException e) {
                            Log.e(TAG, "onAddRequested: ", e);
                        }
                    }
                }
            });
        }

        @Override
        public void onAddParticipants(Object[] args) {
            if (args != null && args.length > 0 && args[0] != null) {
                runOnUiThread(() -> {
                    try {
                        applySeatParticipantFromPayload(new JSONObject(args[0].toString()));
                    } catch (JSONException e) {
                        Log.e(TAG, "onAddParticipants: ", e);
                    }
                });
            }
        }

        @Override
        public void onLessParticipants(Object[] args) {
            if (args != null && args.length > 0 && args[0] != null) {
                runOnUiThread(() -> {
                    try {
                        JSONObject payload = new JSONObject(args[0].toString());
                        clearSeatFromPayload(payload);
                    } catch (JSONException e) {
                        Log.e(TAG, "onLessParticipants: ", e);
                    }
                });
            }
        }

        @Override
        public void onMuteSeat(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args[0] != null) {
                        Log.d(TAG, "run: onMuteSeat ==> " + args[0].toString());
                        try {
                            JSONObject jsonObject = new JSONObject(args[0].toString());
                            if (jsonObject.getInt("position") == -1) {
                                int mute = (jsonObject.getInt("mute"));
//                                viewModel.isMuted = mute != 0;

                                if (jsonObject.getInt("mute") == 1 || jsonObject.getInt("mute") == 2) {
                                    binding.ivMute.setVisibility(View.VISIBLE);
                                } else {
                                    rtcEngine().setEnableSpeakerphone(true);

                                    binding.ivMute.setVisibility(View.GONE);
                                }
                            } else {
                                applySeatMuteFromPayload(jsonObject);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void onLockSeat(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onLockSeat: ==> " + args[0].toString());
                    if (args[0] != null) {
                        try {
                            JSONObject payload = new JSONObject(args[0].toString());
                            applySeatLockFromPayload(payload);
                        } catch (JSONException e) {
                            Log.e(TAG, "onLockSeat: ", e);
                        }
                    }
                }
            });
        }

        @Override
        public void onChangeTheme(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Log.d(TAG, "call: onChangeTheme ==> " + args[0].toString());

                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        String image = jsonObject.getString("background");
                        Glide.with(HostLiveAudioActivity.this).load(ImageUrlUtil.normalize(image)).placeholder(R.drawable.default_bg_audioroom).into(binding.mainImg);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onSeat(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args[0] != null) {
                        String data = args[0].toString();
                        JsonParser parser = new JsonParser();
                        JsonElement mJson = parser.parse(data);
                        Log.d(TAG, "run: onSeat ==> " + data);
                        Gson gson = new Gson();
                        liveUser = gson.fromJson(mJson, PkAudioLiveUserRoot.UsersItem.class);
                        bookedSeatItemList = liveUser.getSeat();
                        binding.tvRcoins.setText(RayziUtils.formatCoin(liveUser.getRCoin()));

                        int hostMute = liveUser.getAudioRoomConfig() != null ? liveUser.getAudioRoomConfig().isHostMute() : 0;
                        Log.d(TAG, "run: ====seat mute + " + hostMute);

                        if (viewModel.isMuted) {
                            binding.ivMute.setVisibility(View.VISIBLE);
                        } else {
//                            rtcEngine().setEnableSpeakerphone(true);
//
//                            AudioManager audioManager;
//                            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                            if (audioManager != null) {
//                                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//                                audioManager.setSpeakerphoneOn(true);
//                            }
                            configureAudioRouting(rtcEngine());

                            binding.ivMute.setVisibility(GONE);
                        }

                        if (liveUser.getSeat().size() >= 15) {
                            gridLayoutManager.setSpanCount(5); // Change to 5 columns when item count is 16 or more
                        } else {
                            gridLayoutManager.setSpanCount(4); // Default back to 4 columns for less than 16 items
                        }

                        seatAdapter.updateData(liveUser.getSeat());
                        finishSeatChange();
                    }
                }
            });

        }

        @Override
        public void onBlock(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Object data = args[0];
                    Log.d(TAG, "onBlock: ==> " + data.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray blockedList = jsonObject.getJSONArray("blocked");
                        for (int i = 0; i < blockedList.length(); i++) {
                            Log.d(TAG, "block user : " + blockedList.get(i).toString());
                            if (blockedList.get(i).toString().equals(sessionManager.getUser().getId())) {
                                Toast.makeText(HostLiveAudioActivity.this, getString(R.string.you_are_blocked_by_host), Toast.LENGTH_SHORT).show();
                                new Handler(Looper.myLooper()).postDelayed(() -> confirmedEndLive(), 500);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }


        @Override
        public void onBanned(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Object data = args[0];
                    Log.d(TAG, "onBanned: ==> " + data.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray blockedList = jsonObject.getJSONArray("blocked");
                        for (int i = 0; i < blockedList.length(); i++) {
                            String blockedId = blockedList.optString(i, "");

                            Log.d(TAG, "onBanned: " + blockedId.toString());
                            String blockedUserId = jsonObject.optString("blockedUserId", "");
                            String currentUserId = sessionManager.getUser().getId();

                            if (blockedUserId.equals(currentUserId)) {
                                if (binding != null && !isFinishing()) {
//                                        Toast.makeText(HostPKLiveActivity.this, R.string.you_are_blocked_by_host, Toast.LENGTH_SHORT).show();
                                    new Handler(Looper.myLooper()).postDelayed(() -> endLive(), 500);
                                }
                            }
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "onBanned: ", e);
                    }
                }
            });
        }

        @Override
        public void onBannedUserList(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args[0] != null) {
                        Log.d(TAG, "onBannedUserList: ==> " + args[0].toString());
                        try {
                            blockUserList = new JSONArray(args[0].toString());
                        } catch (JSONException e) {
                            Log.e(TAG, "onBannedUserList: ", e);
                        }
                    }
                }
            });
        }

        @Override
        public void onBlockUserAlert(Object[] args) {
            runOnUiThread(() -> {
                Log.d(TAG, "onBlock: ==> " + args[0].toString());
                Toast.makeText(HostLiveAudioActivity.this, getString(R.string.you_are_blocked_by_host), Toast.LENGTH_SHORT).show();
                confirmedEndLive();
            });
        }

        @Override
        public void onGetUser(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    String data = args[0].toString();
                    Log.d(TAG, "onGetUser: ==> " + data);
                    JsonParser parser = new JsonParser();
                    JsonElement mJson = parser.parse(data);
                    Gson gson = new Gson();
                    GuestProfileRoot.User userData = gson.fromJson(mJson, GuestProfileRoot.User.class);

                    if (userData != null) {
                        doUserTask(userData, userListPosition, viewerListItem);
                    }
                    customDialogClass.dismiss();
                }
            });
        }

        @Override
        public void onGetUser2(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    String data = args[0].toString();
                    Log.d(TAG, "onGetUser2: ==> " + data);
                    JsonParser parser = new JsonParser();
                    JsonElement mJson = parser.parse(data);
                    Gson gson = new Gson();
                    GuestProfileRoot.User userData = gson.fromJson(mJson, GuestProfileRoot.User.class);

                    if (userData != null) {
                        if (!isFinishing()) {
                            if (userData.getUserId().equals(liveUser.getLiveUserId())) {
                                userProfileBottomSheet.show(false, userData, liveUser.getLiveStreamingId(), false);
                            } else {
                                userProfileBottomSheet.show(false, userData, "", false);
                            }
                        }
                    }
                    customDialogClass.dismiss();
                }
            });

        }

        @Override
        public void onInvite(Object[] args) {

        }

        @Override
        public void onLiveEnd(Object[] args) {

        }

        @Override
        public void onReactionReceived(Object[] args1) {
            handleReactionReceived(args1);
        }

        @Override
        public void onRoomNameChange(Object[] args) {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onRoomNameChange: ==> " + args[0].toString());
                    JSONObject jsonObject = new JSONObject(args[0].toString());
                    binding.tvName.setText(jsonObject.getString("roomName"));
                    liveUser.setRoomName(jsonObject.getString("roomName"));
                } catch (JSONException e) {
                    Log.e(TAG, "onRoomNameChange: ", e);
                }
            });
        }

        @Override
        public void onRoomImageChange(Object[] args) {
            runOnUiThread(() -> {
                Log.d(TAG, "onRoomImageChange: ==> " + args[0].toString());
                liveUser.setRoomImage(args[0].toString());
                Glide.with(HostLiveAudioActivity.this).load(ImageUrlUtil.normalize(args[0].toString())).into(binding.imgProfile);
            });
        }
    };

    private void handleReactionReceived(Object[] args1) {

        runOnUiThread(() -> {
            if (args1[0] != null) {
                try {
                    Log.d(TAG, "onReactionReceived: ==> " + args1[0].toString());
                    JSONObject jsonObject = new JSONObject(args1[0].toString());
                    String imageUrl = jsonObject.getString("image");
                    int rawPosition = jsonObject.optInt("position", -1);
                    if (rawPosition == -1) {
                        UserRoot.User user = new Gson().fromJson(jsonObject.getString("user"), UserRoot.User.class);
                        LiveStramComment liveStramComment = new LiveStramComment("", user, false, liveUser.getLiveStreamingId(), jsonObject.getString("image"), "reaction", "");
                        viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
                        scrollAdapterLogic();

                    } else if (rawPosition == -2) {
                        hostReaction.setReaction(binding.imgHostReaction, imageUrl, 7000, this);

                        Log.d(TAG, "handleReactionReceived: ===> " + rawPosition);
                    } else {

                        int position1 = seatIndexFromPayload(jsonObject, liveUser != null ? liveUser.getSeat() : null);
                        if (position1 < 0 || position1 >= seatAdapter.getList().size()) return;
                        String reactionImage = jsonObject.getString("image");
                        int agoraUid = seatAdapter.getList().get(position1).getAgoraUid();
                        seatAdapter.setReaction(agoraUid, reactionImage);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "handleReactionReceived: ", e);
                }
            }
        });
    }

    private static class ReactionRunner {
        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable currentTask;
        boolean isRunning;

        void setReaction(ImageView iv, String imageUrl, int duration, Context ctx) {
            // Cancel any ongoing reaction
            if (isRunning && currentTask != null) {
                handler.removeCallbacks(currentTask);
                iv.setImageDrawable(null);
                isRunning = false;
            }

            isRunning = true;
            Glide.with(ctx)
                    .asGif()
                    .load(imageUrl)
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<GifDrawable> target, boolean isFirstResource) {
                            isRunning = false;
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull GifDrawable resource, @NonNull Object model, Target<GifDrawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            iv.setImageDrawable(resource);
                            resource.start();

                            currentTask = () -> {
                                iv.setImageDrawable(null);
                                isRunning = false;
                            };
                            handler.postDelayed(currentTask, duration);
                            return true;
                        }
                    })
                    .into(iv);
        }
    }

    private void scrollAdapterLogic() {
        binding.rvComments.scrollToPosition(0);
    }

    private void putLiveRoomKeys(JSONObject jsonObject) throws JSONException {
        jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
        jsonObject.put("liveHistoryId", liveUser.getLiveStreamingId());
        jsonObject.put("liveUserMongoId", liveUser.getId());
    }

    private void putLiveRoomKeys(JsonObject jsonObject) {
        jsonObject.addProperty("liveStreamingId", liveUser.getLiveStreamingId());
        jsonObject.addProperty("liveHistoryId", liveUser.getLiveStreamingId());
        jsonObject.addProperty("liveUserMongoId", liveUser.getId());
    }

    private void putSeatIndex(JsonObject jsonObject, int adapterIndex) {
        jsonObject.addProperty("position", adapterIndex);
        jsonObject.addProperty("seatIndex", adapterIndex);
        if (liveUser != null && liveUser.getSeat() != null && adapterIndex >= 0 && adapterIndex < liveUser.getSeat().size()) {
            jsonObject.addProperty("seatPosition", liveUser.getSeat().get(adapterIndex).getPosition());
        }
    }

    private int seatIndexFromPayload(JSONObject payload, List<PkAudioLiveUserRoot.UsersItem.SeatItem> seats) {
        if (payload == null || seats == null) return -1;
        int index = payload.optInt("seatIndex", -1);
        if (index >= 0 && index < seats.size()) return index;
        int position = payload.optInt("position", -1);
        if (position >= 0 && position < seats.size()) return position;
        for (int i = 0; i < seats.size(); i++) {
            if (seats.get(i).getPosition() == position) return i;
        }
        return -1;
    }

    private boolean beginSeatChange() {
        if (seatChangePending) return false;
        seatChangePending = true;
        seatChangeHandler.removeCallbacks(clearSeatChangePending);
        seatChangeHandler.postDelayed(clearSeatChangePending, 4000);
        return true;
    }

    private void finishSeatChange() {
        seatChangePending = false;
        seatChangeHandler.removeCallbacks(clearSeatChangePending);
    }

    @Override
    public void onBackPressed() {
        endLive();
    }

    /** Confirm end vs. background-minimize room; updates engine mute + floating service */
    private void endLive() {
        Log.d(TAG, "endLive: ==== ");
        if (!isFinishing()) {
            new PopupBuilder(this).showLiveEndPopup(new PopupBuilder.OnMultiButtonPopupLister() {
                @Override
                public void onClickContinue() {
//                    PkAudioLiveUserRoot.UsersItem.AudioRoomConfig audioRoomConfig = new PkAudioLiveUserRoot.UsersItem.AudioRoomConfig();
//                    audioRoomConfig.setHostMute(viewModel.isMuted ? 1 : 1);
                    if (rtcEngine() != null) {
                        rtcEngine().muteLocalAudioStream(viewModel.isMuted);
                    }
                    Log.d(TAG, "onClickCancel: === isHostMute" + viewModel.isMuted);
                    confirmedEndLive();
                }

                @Override
                public void onClickCancel() {
                    if (checkOverlayDisplayPermission()) {
                        sessionManager.saveBooleanValue("isHostKeep", true);
                        startService(new Intent(HostLiveAudioActivity.this, FloatingButtonService.class).putExtra("image", sessionManager.getUser().getImage()));
                        finish();
                    } else {
                        requestOverlayDisplayPermission();
                    }
                    if (rtcEngine() != null) {
                        rtcEngine().muteLocalAudioStream(viewModel.isMuted);
                        Log.d(TAG, "onClickCancel: =======mute value : " + viewModel.isMuted);
                    }

                    sessionManager.saveLiveUserForBackground(liveUser);
                    sessionManager.setIsAudioRoomBackground(true);
                    sessionManager.setIsAudioRoomExit(false);
                }
            });
        }
    }


    /** Final tear-down: leave channel, emit server signals, flip session flags, finish() */
    private void confirmedEndLive() {


        JsonObject jsonObject1 = new JsonObject();
        putSeatIndex(jsonObject1, hostPosition);
        putLiveRoomKeys(jsonObject1);
        jsonObject1.addProperty("userId", sessionManager.getUser().getId());

        MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject1);

        forceAudienceListenOnly();
        hostPosition = -1;

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("liveUserId", liveUser.getLiveUserId());
            putLiveRoomKeys(jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "confirmedEndLive: ", e);
        }
        MySocketManager.getInstance().getSocket().emit("audioLiveHostRemove", jsonObject);
        Log.d(TAG, "confirmedEndLive: ===audioLiveHostRemove");

        if (rtcEngine() != null) {
            rtcEngine().leaveChannel();
        }
        AudioMixingController.getInstance().stop();
        sessionManager.saveBooleanValue("isHostKeep", false);
        sessionManager.saveLiveUserForBackground(liveUser);
        sessionManager.setIsAudioRoomBackground(true);
        sessionManager.setIsAudioRoomExit(true);
        BaseActivity.STATUS_LIVE = false;
        finish();

    }

    /** Join Agora audio channel as host: set profile, token, speakerphone, and broadcast mute state */
    private void joinChannel() {
        try {
            if (rtcEngine() != null) {
                rtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
                String tkn = RtcTokenBuilderSample.main(liveUser.getChannel() + "audio", sessionManager.getSetting().getAgoraKey(), sessionManager.getSetting().getAgoraCertificate());
                forceAudienceListenOnly();
                rtcEngine().joinChannel(tkn, liveUser.getChannel() + "audio", "", liveUser.getAgoraUID());
                Log.d("fatal", "onCreate: audio live" + liveUser.getChannel());
                rtcEngine().enableAudioVolumeIndication(1000, 3, true); // To detect who is currently speaking
                forceAudienceListenOnly();
                rtcEngine().setEnableSpeakerphone(true);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void forceAudienceListenOnly() {
        if (rtcEngine() == null) return;
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        rtcEngine().enableAudio();
        rtcEngine().disableVideo();
        rtcEngine().muteLocalAudioStream(true);
        rtcEngine().adjustRecordingSignalVolume(0);
    }

    /** Switch to broadcaster role with audio-only capture*/
    private void startBroadcast() {

        Log.d(TAG, "startBroadcast: ");
        try {
            if (rtcEngine() != null) {
                rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
                rtcEngine().enableAudio();
                rtcEngine().disableVideo();
            }
        } catch (Exception e) {
            Log.e(TAG, "startBroadcast: ", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_host_live_audio);
        MySocketManager.getInstance().addAudioRoomHandler(audioRoomHandler);
        MySocketManager.getInstance().addSocketConnectHandler(socketConnectHandler);
        giftReceiveAdapter = new GiftReceiveAdapter(HostLiveAudioActivity.this);

        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new HostLiveViewModel()).createFor()).get(HostLiveViewModel.class);
        giftViewModel = ViewModelProviders.of(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);
        ReactionsViewModel reactionsViewModel = ViewModelProviders.of(this, new ViewModelFactory(new ReactionsViewModel()).createFor()).get(ReactionsViewModel.class);
        sessionManager = new SessionManager(this);
        hostAPICall = new HostAPICall(this, "audio");
        giftViewModel.initEmojiSheet(this);
        giftViewModel.getGiftCategory();
        binding.setViewModel(viewModel);

        emojiBottomsheetFragment = new EmojiBottomSheetFragment(true);

        bottomSheetReactions = new BottomSheetReactions(this);
        reactionsViewModel.loadReactions(bottomSheetReactions::loadData);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);   // anchor at bottom
        layoutManager.setReverseLayout(true); // natural order
        binding.rvComments.setLayoutManager(layoutManager);
        binding.rvComments.setAdapter(viewModel.liveStramCommentAdapter);
        binding.btnSend.setOnClickListener(this::onClickSendComment);
        binding.commentInputContainer.setOnClickListener(v -> showCommentKeyboard());
        binding.etComment.setOnClickListener(v -> showCommentKeyboard());

        userProfileBottomSheet = new UserProfileBottomSheet(this);
        binding.rvComments.scrollToPosition(
                viewModel.liveStramCommentAdapter.getItemCount() - 1
        );
        viewModel.initLister();

        gridLayoutManager = new GridLayoutManager(this, 4);
        binding.rvSeat.setLayoutManager(gridLayoutManager);
        seatAdapter = new SeatAdapter(HostLiveAudioActivity.this, sessionManager,sessionManager.getUser().getId());
        binding.rvSeat.setAdapter(seatAdapter);

        if (isMyServiceRunning()) {
            stopService(new Intent(HostLiveAudioActivity.this, FloatingButtonService.class));
        }

        Intent intent = getIntent();
        String data = intent != null ? intent.getStringExtra(Const.DATA) : null;

        if (data == null || data.isEmpty()) {
            Log.e(TAG, "Missing Const.DATA in intent; using fallback room data.");
            data = buildFallbackHostAudioRoomJson();
        }

        PkAudioLiveUserRoot.UsersItem parsed = null;
        try {
            JsonElement dataElement = JsonParser.parseString(data);
            if (dataElement.isJsonObject() && dataElement.getAsJsonObject().has("liveUser")) {
                dataElement = dataElement.getAsJsonObject().get("liveUser");
            }
            parsed = new Gson().fromJson(dataElement, PkAudioLiveUserRoot.UsersItem.class);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse live room data JSON, using fallback room data. data=" + data, e);
            try {
                parsed = new Gson().fromJson(buildFallbackHostAudioRoomJson(), PkAudioLiveUserRoot.UsersItem.class);
            } catch (Exception fallbackError) {
                Log.e(TAG, "Failed to parse fallback live room data JSON", fallbackError);
            }
        }

        if (parsed == null) {
            Log.e(TAG, "Parsed liveUser is null after fallback; cannot continue.");
            Toast.makeText(this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        liveUser = parsed;
        liveUser.setAgoraUID(1);
        binding.tvRcoins.setText(RayziUtils.formatCoin(liveUser.getRCoin()));

        if (sessionManager.getIsAudioRoomExit()) {
            Call<RestResponse> call = RetrofitBuilder.create().getNotification(sessionManager.getUser().getId());
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        RestResponse responseBody = response.body();
                        if (responseBody.isStatus()) {
                            Log.d(TAG, "Notifications retrieved successfully.");
                        } else {
                            Log.w(TAG, "Failed to fetch notifications: " + responseBody.getMessage());
                        }
                    } else {
                        Log.w(TAG, "Unsuccessful response, code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<RestResponse> call, Throwable t) {
                    Log.e(TAG, "Error fetching notifications", t);
                }
            });

        }

        Log.d(TAG, "onCreate: live room id " + liveUser.getLiveStreamingId());
        emitHostRoomJoin();
        Log.d(TAG, "onCreate: liveRoomConnect emitted...");
        Log.d(TAG, "onCreate: hostJoinAudioRoom emitted...");

        binding.tvName.setText(liveUser.getRoomName());
        RayziUtils.marqueeText(binding.tvName);

        binding.tvUniqueId.setText(getString(R.string.id) + sessionManager.getUser().getUniqueId());

        // Room image / name
        if (!TextUtils.isEmpty(liveUser.getRoomImage())) {
            Glide.with(this)
                    .load(ImageUrlUtil.normalize(liveUser.getRoomImage()))
                    .apply(MainApplication.requestOptions)
                    .circleCrop()
                    .into(binding.imgProfile);
        }

        binding.mainHostProfileImage.setUserImage(sessionManager.getUser().getImage(), sessionManager.getUser().getAvatarFrameImage(), 20);
        binding.mainHostnameCount.setText(
                !TextUtils.isEmpty(liveUser.getName()) ? liveUser.getName() : getString(R.string.unknown));

        JSONObject jsonObject1 = new JSONObject();
        try {
            putLiveRoomKeys(jsonObject1);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: ", e);
        }
        MySocketManager.getInstance().getSocket().emit(Const.LIVE_BLOCKLIST_UPDATED, jsonObject1);
        Log.d(TAG, "onCreate: LIVE_BLOCKLIST_UPDATED emitted.. " + jsonObject1.toString());

        // Seat list
        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seats = liveUser.getSeat();
        if (seats != null && !seats.isEmpty()) {
            bookedSeatItemList = seats;
            seatAdapter.addData(seats);
        } else {
            seatAdapter.addData(Collections.emptyList());
        }


        rtcStatsView = findViewById(R.id.single_host_rtc_stats);
        initLister();
        joinChannel();
        BaseActivity.STATUS_LIVE = true;
//        startBroadcast();

        // Background image
        String bg = liveUser.getBackground();
        if (!TextUtils.isEmpty(bg)) {
            Glide.with(this)
                    .load(ImageUrlUtil.normalize(bg))
                    .thumbnail(Glide.with(this).load(ImageUrlUtil.normalize(bg)))
                    .placeholder(R.drawable.app_main_bg)
                    .into(binding.mainImg);
        } else {
            Glide.with(this).load(R.drawable.default_bg_audioroom).into(binding.mainImg);
        }

        binding.rvGift.setAdapter(giftReceiveAdapter);

        if (liveUser.getRoomWelcome() != null) {
            viewModel.liveStramCommentAdapter.addSingleComment(null);
            LiveStramComment liveStreamComment1 = new LiveStramComment(getString(R.string.announcement) + liveUser.getRoomWelcome(), sessionManager.getUser(), true, liveUser.getLiveStreamingId(), "", "comment", "");
            viewModel.liveStramCommentAdapter.addSingleComment(liveStreamComment1);
        } else {
            viewModel.liveStramCommentAdapter.addSingleComment(null);
            LiveStramComment liveStreamComment1 = new LiveStramComment(getString(R.string.announcement_welcome_to_room), sessionManager.getUser(), true, liveUser.getLiveStreamingId(), "", "comment", "");
            viewModel.liveStramCommentAdapter.addSingleComment(liveStreamComment1);
        }

        if (sessionManager.getLiveUserForBackground() != null && sessionManager.getLiveUserForBackground().getAudioRoomConfig() != null) {
            int isMuted = sessionManager.getLiveUserForBackground().getAudioRoomConfig().isHostMute();
            Log.e(TAG, "initLister: >>>>>>>>>>>>>  " + viewModel.isMuted);
            JsonObject jsonObject = new JsonObject();
            putSeatIndex(jsonObject, -1);
            putLiveRoomKeys(jsonObject);
            jsonObject.addProperty("liveUserId", liveUser.getLiveUserId());
            Log.d(TAG, "onMuteMic: liveUser.getLiveStreamingId() === " + liveUser.getLiveStreamingId());
            jsonObject.addProperty("agoraId", liveUser.getAgoraUID());

            jsonObject.addProperty("mute", (viewModel.isMuted) ? 1 : 0);
            jsonObject.addProperty("mutedUserId", sessionManager.getUser().getId());
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_MUTE_SEAT, jsonObject);
            Log.d(TAG, "onCreate: hostMute===" + viewModel.isMuted);

            if(viewModel.isMuted){
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_mute));
            }else {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_unmute));
            }

            sessionManager.saveLiveUserForBackground(null);

        }

        setupOptionsToggle();
        autoHideWhenKeyboardShown(findViewById(R.id.lytButtons));
    }

    /** Push comment via socket and optimistically add to adapter (clears input) */
    public void onClickSendComment(View view) {
        String comment = binding.etComment.getText().toString();
        if (!comment.isEmpty()) {
            binding.etComment.setText("");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("comment", comment);
                jsonObject.put("user", new Gson().toJson(sessionManager.getUser()));
                jsonObject.put("isJoined", false);
                putLiveRoomKeys(jsonObject);
                jsonObject.put("userId", sessionManager.getUser().getId());
            } catch (JSONException e) {
                Log.e(TAG, "onClickSendComment: ", e);
            }
            LiveStramComment liveStramComment = new LiveStramComment(comment, sessionManager.getUser(), false, liveUser.getLiveStreamingId(), "", "comment", "");
            rememberLocalComment(comment);
            viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
            scrollAdapterLogic();
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_COMMENT_AUDIO, jsonObject);
        }
    }

    private void showCommentKeyboard() {
        binding.etComment.post(() -> {
            binding.etComment.setFocusable(true);
            binding.etComment.setFocusableInTouchMode(true);
            binding.etComment.requestFocus();
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.etComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void rememberLocalComment(String comment) {
        lastLocalCommentText = comment == null ? "" : comment;
        lastLocalCommentAt = System.currentTimeMillis();
    }

    private boolean isDuplicateLocalComment(LiveStramComment liveStramComment) {
        if (liveStramComment == null || liveStramComment.getUser() == null || sessionManager.getUser() == null) {
            return false;
        }
        boolean sameUser = liveStramComment.getUser().getId() != null
                && liveStramComment.getUser().getId().equals(sessionManager.getUser().getId());
        boolean sameText = liveStramComment.getComment() != null
                && liveStramComment.getComment().equals(lastLocalCommentText);
        return sameUser && sameText && System.currentTimeMillis() - lastLocalCommentAt < 2500;
    }

    private String buildFallbackHostAudioRoomJson() {
        UserRoot.User user = sessionManager.getUser();
        String userId = user != null && !TextUtils.isEmpty(user.getId()) ? user.getId() : "hiko-demo-user";
        String name = user != null && !TextUtils.isEmpty(user.getName()) ? user.getName() : "HIKO Host";
        String image = user != null && user.getImage() != null ? user.getImage() : "";
        String country = user != null && !TextUtils.isEmpty(user.getCountry()) ? user.getCountry() : "Palestine";
        String username = user != null && !TextUtils.isEmpty(user.getUsername()) ? user.getUsername() : "hiko_host";
        String uniqueId = user != null && !TextUtils.isEmpty(user.getUniqueId()) ? user.getUniqueId() : "100001";
        double rCoin = user != null ? user.getrCoin() : 0;
        double diamond = user != null ? user.getDiamond() : 0;
        int age = user != null ? user.getAge() : 18;
        String roomId = userId + System.currentTimeMillis();

        JsonArray seats = new JsonArray();
        for (int i = 0; i < 15; i++) {
            JsonObject seat = new JsonObject();
            seat.addProperty("position", i + 1);
            seat.addProperty("name", "");
            seat.addProperty("image", "");
            seat.addProperty("country", "");
            seat.addProperty("reserved", false);
            seat.addProperty("mute", 0);
            seat.addProperty("lock", false);
            seat.addProperty("agoraUid", 0);
            seat.addProperty("userId", "");
            seat.addProperty("coin", 0);
            seat.addProperty("isHost", false);
            seats.add(seat);
        }

        JsonObject audioConfig = new JsonObject();
        audioConfig.addProperty("isHostMute", 0);

        JsonObject liveUser = new JsonObject();
        liveUser.addProperty("_id", roomId);
        liveUser.addProperty("id", roomId);
        liveUser.addProperty("liveUserId", userId);
        liveUser.addProperty("liveStreamingId", roomId);
        liveUser.addProperty("channel", userId);
        liveUser.addProperty("agoraUID", 1);
        liveUser.addProperty("token", "");
        liveUser.addProperty("country", country);
        liveUser.addProperty("image", image);
        liveUser.addProperty("rCoin", rCoin);
        liveUser.addProperty("diamond", diamond);
        liveUser.addProperty("name", name);
        liveUser.addProperty("username", username);
        liveUser.addProperty("uniqueId", uniqueId);
        liveUser.addProperty("isVIP", true);
        liveUser.addProperty("isPublic", true);
        liveUser.addProperty("audio", true);
        liveUser.addProperty("age", age);
        liveUser.addProperty("view", 0);
        liveUser.addProperty("roomImage", image);
        liveUser.addProperty("roomName", "HIKO Live Room");
        liveUser.addProperty("roomWelcome", getString(R.string.welcome_to_the_party));
        liveUser.addProperty("privateCode", 0);
        liveUser.addProperty("roomOwnerUniqueId", uniqueId);
        liveUser.add("seat", seats);
        liveUser.addProperty("background", "");
        liveUser.addProperty("filter", "");
        liveUser.addProperty("isPkMode", false);
        liveUser.addProperty("pkView", false);
        liveUser.addProperty("disconnect", false);
        liveUser.addProperty("duration", 0);
        liveUser.add("audioConfig", audioConfig);
        return liveUser.toString();
    }

    private void initLister() {


        binding.imgMusic.setOnClickListener(v -> {
            confirmedSongs = loadConfirmedSongs();
            checkAndProceed();
        });

        viewModel.clickedComment.observe(this, user -> {
            getUser2(user.getId());
        });

        viewModel.clickedUser.observe(this, user -> {
            try {
                getUser2(user.get("userId").toString());
            } catch (JSONException e) {
                Log.e(TAG, "initLister: ", e);
            }
        });

        binding.imgGame.setOnClickListener(v -> {
            new BottomSheetGameList(this, (gameItem, position) -> {
                if (position == 0) {
                    new BottomSheetGameCasino(this, gameItem.getLink(), () -> {
                        MySocketManager.getInstance().getSocket().emit(Const.USER_COIN_UPDATE, sessionManager.getUser().getId());
                        Log.d(TAG, "onDismiss: counts:..." + sessionManager.getUser().getDiamond());
                    });
                } else if (position == 1) {
                    new DialogGame(this, gameItem.getLink(), () ->
                            MySocketManager.getInstance().getSocket().emit(Const.USER_COIN_UPDATE, sessionManager.getUser().getId()));
                } else {
                    new BottomSheetGameTeenPatti(this, gameItem.getLink(), () ->
                            MySocketManager.getInstance().getSocket().emit(Const.USER_COIN_UPDATE, sessionManager.getUser().getId()));
                }
            });
        });


        binding.btnMute.setOnClickListener(v -> {
            if (rtcEngine() != null) {
                if (hostPosition == -1 || getSelfPositionFromSeat() == null) {
                    forceAudienceListenOnly();
                    binding.btnMute.setImageDrawable(ContextCompat.getDrawable(HostLiveAudioActivity.this, R.drawable.ic_mute));
                    Toast.makeText(this, "Choose a seat first", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.isMuted = !viewModel.isMuted;
                rtcEngine().muteLocalAudioStream(viewModel.isMuted);
                Log.e(TAG, "initLister: >>>>>>>>>>>>>  " + viewModel.isMuted);
                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, hostPosition);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("liveUserId", liveUser.getLiveUserId());
                Log.d(TAG, "onMuteMic: liveUser.getLiveStreamingId() === " + liveUser.getLiveStreamingId());
                jsonObject.addProperty("agoraId", liveUser.getAgoraUID());
                jsonObject.addProperty("mute", (viewModel.isMuted) ? 1 : 0);
                jsonObject.addProperty("mutedUserId", sessionManager.getUser().getId());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_MUTE_SEAT, jsonObject);
                if (viewModel.isMuted) {
                    rtcEngine().adjustRecordingSignalVolume(0);
                    binding.btnMute.setImageDrawable(ContextCompat.getDrawable(HostLiveAudioActivity.this, R.drawable.ic_mute));
                } else {
                    rtcEngine().adjustRecordingSignalVolume(100);
                    binding.btnMute.setImageDrawable(ContextCompat.getDrawable(HostLiveAudioActivity.this, R.drawable.ic_unmute));
                }
            }
        });

        binding.btnMuteAllSeats.setOnClickListener(v -> muteAllGuestSeats());


        binding.btnSetting.setOnClickListener(v -> {
            if (HostLiveAudioActivity.this.isFinishing() || HostLiveAudioActivity.this.isDestroyed()) {
                return;
            }

            new BottomSheetAudioRoomSetting(HostLiveAudioActivity.this, liveUser, new BottomSheetAudioRoomSetting.RoomSettingListener() {
                @Override
                public void onRoomNameChanged(BottomSheetAudioroomSettingsBinding audioRoomSettingsBinding) {
                    new BottomSheetAudioRoomName(HostLiveAudioActivity.this, liveUser, audioRoomSettingsBinding.tvName::setText);
                }

                @Override
                public void onRoomImageChanged(BottomSheetAudioroomSettingsBinding audioRoomSettingsBinding) {
                    launchPhotoPicker();
                }

                @Override
                public void onSeatSizeChanged(BottomSheetAudioroomSettingsBinding audioRoomSettingsBinding) {
                    new BottomSheetAudioRoomWheatMode(HostLiveAudioActivity.this, liveUser.getSeat().size(), new BottomSheetAudioRoomWheatMode.OnSeatClickListener() {
                        @Override
                        public void onSeatClick(int seatCount) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                putLiveRoomKeys(jsonObject);
                                jsonObject.put("seatCount", seatCount);
                            } catch (JSONException e) {
                                Log.e(TAG, "onSeatClick: ", e);
                            }
                            MySocketManager.getInstance().getSocket().emit(Const.EVENT_UPDATE_SEAT_COUNT, jsonObject);
                            Toast.makeText(HostLiveAudioActivity.this, getString(R.string.seat_update), Toast.LENGTH_SHORT).show();
                            audioRoomSettingsBinding.tvSeatCount.setText(seatCount + getString(R.string.people));
                        }
                    });
                }

                @Override
                public void onRoomWelcomeMessageChanged(BottomSheetAudioroomSettingsBinding audioRoomSettings) {
                    new BottomSheetAudioRoomWelcomeMsg(HostLiveAudioActivity.this, liveUser, text -> {
                        audioRoomSettings.tvWelcomeMsg.setText(text);
                        liveUser.setRoomWelcome(text);
                        Log.d(TAG, "onRoomWelcomeMessageChanged: ==> " + liveUser.getRoomWelcome());
                    });
                }

                @Override
                public void onRoomPasscodeChanged(BottomSheetAudioroomSettingsBinding audioRoomSettings) {
                    new BottomSheetAudioRoomChangePasscode(HostLiveAudioActivity.this, liveUser, RoomPasscode -> {
                        audioRoomSettings.tvPassCode.setText(RoomPasscode);
                        liveUser.setPrivateCode(Integer.parseInt(RoomPasscode));
                    });

                }

                @Override
                public void onRoomBackgroundChanged() {
                    new BottomSheetOptions(HostLiveAudioActivity.this, image -> {

                        Glide.with(HostLiveAudioActivity.this).load(ImageUrlUtil.normalize(image)).into(binding.mainImg);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            putLiveRoomKeys(jsonObject);
                            jsonObject.put("background", image);
                            jsonObject.put("isDefault", true);

                            Log.d(TAG, "onRoomBackgroundChanged:  ====> " + image);

                            liveUser.setBackground(image);
                        } catch (JSONException e) {
                            Log.e(TAG, "onRoomBackgroundChanged: ", e);
                        }
                        MySocketManager.getInstance().getSocket().emit(Const.EVENT_CHANGE_THEME, jsonObject);
                    });
                }

                @Override
                public void onBannedUser() {
                    Log.d(TAG, "Blocked Users List: " + blockUserList.toString());

                    new BottomSheetBannedList(HostLiveAudioActivity.this, blockUserList, new BottomSheetBannedList.OnclickListener() {
                        @Override
                        public void onUnblockClick(String id) {
                            try {

                                JSONObject jsonObject1 = new JSONObject();
                                jsonObject1.put("blocked", blockedUsersList);
                                jsonObject1.put("type", "unblock");
                                putLiveRoomKeys(jsonObject1);
                                jsonObject1.put("blockedUserId", id);
                                MySocketManager.getInstance().getSocket().emit(Const.EVENT_UPDATE_BLOCKED_LIST, jsonObject1);
                            } catch (JSONException e) {
                                Log.e(TAG, "onUnblockClick: ", e);
                            }
                        }
                    });
                }

                @Override
                public void onRoomClose() {
                    new PopupBuilder(HostLiveAudioActivity.this).deletePopup(getString(R.string.are_you_sure_you_want_to_delete_your_room), new PopupBuilder.OnMultiButtonPopupLister() {
                        @Override
                        public void onClickContinue() {
                            RetrofitBuilder.create().deleteRoom(sessionManager.getUser().getId()).enqueue(new Callback<RestResponse>() {
                                @Override
                                public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                        confirmedEndLive();
                                        Toast.makeText(HostLiveAudioActivity.this, getString(R.string.your_room_deleted_successfully), Toast.LENGTH_SHORT).show();
                                        sessionManager.setIsAudioRoomBackground(false);
                                        Log.d(TAG, "Room deleted successfully.");
                                    } else {
                                        Log.w(TAG, "Failed to delete room: " + (response.body() != null ? response.body().getMessage() : "No message"));
                                        Toast.makeText(HostLiveAudioActivity.this, getString(R.string.failed_to_delete_room), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<RestResponse> call, Throwable t) {
                                    Log.e(TAG, "Error deleting room", t);
                                    Toast.makeText(HostLiveAudioActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onClickCancel() {

                        }
                    });
                }
            });
        });

        binding.btnClose.setOnClickListener(v -> endLive());

        binding.ivShare.setOnClickListener(v -> {
            binding.ivShare.setEnabled(false);

            String deepLink = BuildConfig.BASE_URL + "open?type=AUDIO_LIVE&userId=" + liveUser.getLiveUserId() + "&joinUserId=" + sessionManager.getUser().getId() + "&liveStreamingId=" + liveUser.getLiveStreamingId() + "&livetype=audio";

            Log.d(TAG, "onShareClick: ==" + deepLink);

            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, deepLink);
                startActivity(Intent.createChooser(shareIntent, "Share"));
            } catch (Exception e) {
                Log.e(TAG, "Share error: " + e.getMessage());
            }


        });

        binding.btnReaction.setOnClickListener(view -> bottomSheetReactions.show());
        bottomSheetReactions.setOnReactionClickListener(reaction -> {
            Log.d(TAG, "initLister: " + reaction.getImage());
            try {
                JSONObject jsonObject = new JSONObject();
                putLiveRoomKeys(jsonObject);
                jsonObject.put("position", (hostPosition == -1) ? -1 : hostPosition); // for host
                jsonObject.put("seatIndex", (hostPosition == -1) ? -1 : hostPosition);
                if (liveUser != null && liveUser.getSeat() != null && hostPosition >= 0 && hostPosition < liveUser.getSeat().size()) {
                    jsonObject.put("seatPosition", liveUser.getSeat().get(hostPosition).getPosition());
                }
                jsonObject.put("image", reaction.getImage());
                jsonObject.put("user", new Gson().toJson(sessionManager.getUser()));
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_SEND_REACTION, jsonObject);
            } catch (Exception o) {
                o.printStackTrace();
            }
        });

        giftViewModel.finalGift.observe(this, giftItem -> {
            if (giftItem != null) {
                double totalCoin = giftItem.getCoin() * giftItem.getCount();
                if (sessionManager.getUser().getDiamond() < totalCoin) {
                    Toast.makeText(HostLiveAudioActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    if (!giftViewModel.userListAdapter.getCurrentList().isEmpty()) {
                        List<String> selectedUsers = giftViewModel.userListAdapter.getCurrentList().stream().filter(UserSelectableClass::isSelected).map(user -> user.getSeatItem().getUserId()).collect(Collectors.toList());

                        List<String> selectedUsersName = giftViewModel.userListAdapter.getCurrentList().stream()
                                .filter(UserSelectableClass::isSelected)
                                .map(user -> user.getSeatItem().getName())
                                .collect(Collectors.toList());
                        if (selectedUsers.isEmpty()) {
                            Toast.makeText(this, getString(R.string.select_at_least_one_user), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("senderUserId", sessionManager.getUser().getId());
                        jsonObject.put("receiverUserId", Arrays.toString(selectedUsers.toArray()));
                        putLiveRoomKeys(jsonObject);
                        jsonObject.put("hostId", liveUser.getLiveUserId());
                        jsonObject.put("userName", sessionManager.getUser().getName());
                        jsonObject.put("receiverUserName", String.join(",", selectedUsersName));
                        jsonObject.put("coin", giftItem.getCoin() * giftItem.getCount());
                        jsonObject.put("gift", new Gson().toJson(giftItem));
                        jsonObject.put("giftCount", giftItem.getCount());
                        jsonObject.put("timeStamp", System.currentTimeMillis());
                        jsonObject.put("liveType", "audio");
                        int i = selectedUsers.size();
                        double totalGiftCoin = giftItem.getCoin() * giftItem.getCount() * i;
                        double totalDiamond = sessionManager.getUser().getDiamond();

                        Log.d(TAG, "receiverUserId: " + i);
                        Log.d(TAG, "totalGiftCoin: " + totalGiftCoin);
                        Log.d(TAG, "totalDiamond: " + totalDiamond);
                        if (totalDiamond >= totalGiftCoin) {
                            MySocketManager.getInstance().getSocket().emit(Const.EVENT_NORMAL_USER_GIFT, jsonObject);
                        } else {
                            Toast.makeText(HostLiveAudioActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.don_t_have_user_to_sent_a_gift_wait_for_user), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        seatAdapter.setOnSeatClick((seatItem, position) -> {
            doWork(seatItem, position);
        });


        userProfileBottomSheet.setOnUserTapListener(userDummy -> {
            blockedUsersList.put(userDummy.getUserId());

            try {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("blocked", blockedUsersList);
                jsonObject1.put("type", "block");
                putLiveRoomKeys(jsonObject1);
                jsonObject1.put("blockedUserId", userDummy.getUserId());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_UPDATE_BLOCKED_LIST, jsonObject1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


//        binding.options.setOnClickListener(v -> {
//            new BottomSheetOptions(HostLiveAudioActivity.this, image -> {
//                Glide.with(HostLiveAudioActivity.this).load(BuildConfig.BASE_URL + image).into(binding.mainImg);
//
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("liveUserMongoId", liveUser.getId());
//                    jsonObject.put("background", image);
//                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
//                    sessionManager.saveStringValue(Const.THEME_STR, image);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                MySocketManager.getInstance().getSocket().emit(Const.EVENT_CHANGE_THEME, jsonObject);
//                Log.d(TAG, "onGalleryClick: " + jsonObject.toString());
//
//            });
//
//        });

    }

    /** Android 13 photo picker (fallback to legacy intent for older OS)*/
    private void launchPhotoPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            // Fallback for pre-Android 13
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }
    }

    /** Seat tile click: self-remove / inspect user / open mic actions depending on state */
    private void doWork(PkAudioLiveUserRoot.UsersItem.SeatItem seatItem, int i) {
        Log.e(TAG, "doWork: >>>>>>>>>>  " + i);

        if (seatItem.isReserved() && seatItem.getUserId() != null && seatItem.getUserId().equalsIgnoreCase(sessionManager.getUser().getId())) {
            new PopupBuilder(this).showRemovePopup(() -> {
                if (!beginSeatChange()) return;
                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, i);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("userId", sessionManager.getUser().getId());

                MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);
                clearSeatAt(i);
                forceAudienceListenOnly();
                hostPosition = -1;
                Log.d(TAG, "doWork: remove sit by it self" + jsonObject.toString());

            });
            return;
        }

        if (seatItem.isReserved()) {
            getUser(seatItem.getUserId(), i, seatItem);
            return;
        }

        new BottomSheetHostMic(HostLiveAudioActivity.this, seatItem, new BottomSheetHostMic.OnClickListener() {
            @Override
            public void onTakeMic() {
                if (!beginSeatChange()) return;

                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, i);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("userId", sessionManager.getUser().getId());
                jsonObject.addProperty("name", sessionManager.getUser().getName());
                jsonObject.addProperty("country", sessionManager.getUser().getCountry());
                jsonObject.addProperty("agoraUid", liveUser.getAgoraUID());

                int currentState = seatItem.isMute();
                if (currentState == 0) {
                    currentState = viewModel.isMuted ? 1 : 0;
                }
                PkAudioLiveUserRoot.UsersItem.SeatItem selfPos = getSelfPositionFromSeat();
                if (selfPos != null && selfPos.isMute() == 2) {
                    currentState = 2;
                }

                jsonObject.addProperty("mute", currentState);
                jsonObject.addProperty("image", sessionManager.getUser().getImage());
                jsonObject.addProperty("avatarFrame", sessionManager.getUser().getAvatarFrameImage());

                if (selfPos != null && selfPos != seatItem) {
                    clearSeatItem(selfPos);
                }
                reserveSeatForCurrentUser(seatItem, currentState, liveUser.getAgoraUID());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_ADD_PARTICIPATED, jsonObject);
                Log.d(TAG, "doWork: add participate emit " + jsonObject);


                becomeHost(seatItem, true);
                hostPosition = i;
                Log.d(TAG, "doWork: CURRENT STATE MUTE : " + currentState);

                if (currentState == 2) {
                    viewModel.isMuted = true;
                    binding.btnMute.setEnabled(false);
                    binding.btnMute.setImageDrawable(ContextCompat.getDrawable(HostLiveAudioActivity.this, R.drawable.mute_blocked));
                } else if (currentState == 1) {
                    viewModel.isMuted = true;
                    binding.btnMute.setEnabled(true);
                    binding.btnMute.setImageDrawable(ContextCompat.getDrawable(HostLiveAudioActivity.this, R.drawable.ic_mute));
                } else {
                    viewModel.isMuted = false;
                    binding.btnMute.setEnabled(true);
                    binding.btnMute.setImageDrawable(ContextCompat.getDrawable(HostLiveAudioActivity.this, R.drawable.ic_unmute));
                }
                if (rtcEngine() != null) {
                    rtcEngine().muteLocalAudioStream(viewModel.isMuted);
                    rtcEngine().adjustRecordingSignalVolume(viewModel.isMuted ? 0 : 100);
                }


            }

            @Override
            public void onGiveMic() {
                if (finalArray != null && finalArray.length() > 0) {
                    Log.e(TAG, "onGiveMic: Click>>>>>>>>>>>>>>>>>>>>>> if condition");

                    new BottomSheetViewersUsers(HostLiveAudioActivity.this, finalArray, userDummy -> {
                        try {
                            Log.e(TAG, "onGiveMic: >>>>>>>>>>>>>>>try catch");
                            Log.d(TAG, "onGiveMic: userDummy.toString() ==  " + userDummy.toString());
                            Log.d(TAG, "onGiveMic: userDummy.toString() ==  i " + i);
                            Log.d(TAG, "onGiveMic: userDummy.toString() ==  liveUser.getId() " + liveUser.getId());
                            Log.d(TAG, "onGiveMic: userDummy.toString() ==  liveUser.getLiveStreamingId() " + liveUser.getLiveStreamingId());
                            JsonObject jsonObject = new JsonObject();
                            putSeatIndex(jsonObject, i);
                            putLiveRoomKeys(jsonObject);
                            jsonObject.addProperty("userId", userDummy.get("userId").toString());
                            jsonObject.addProperty("name", userDummy.get("name").toString());
                            jsonObject.addProperty("country", userDummy.get("country").toString());
                            jsonObject.addProperty("agoraUid", -1);
                            jsonObject.addProperty("mute", 0);
                            jsonObject.addProperty("request", false);
                            jsonObject.addProperty("image", userDummy.get("image").toString());
                            jsonObject.addProperty("avatarFrame", userDummy.get("avatarFrameImage").toString());
                            MySocketManager.getInstance().getSocket().emit(Const.EVENT_ADD_REQUESTED, jsonObject);
                            Log.d(TAG, "onGiveMic: emit(Const.EVENT_ADD_REQUESTED=== " + jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "onGiveMic: catch  == " + e.getMessage());
                        }
                    });
                } else {
                    Toast.makeText(HostLiveAudioActivity.this, getString(R.string.there_s_no_user_to_invite_on_mic), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onLockMic() {
                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, i);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("lock", !seatItem.isLock());
                seatItem.setLock(!seatItem.isLock());
                seatAdapter.updateData(liveUser.getSeat());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_LOCK_SEAT, jsonObject);
            }

            @Override
            public void onMuteMic() {
                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, i);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("liveUserId", liveUser.getLiveUserId());
                Log.d(TAG, "onMuteMic: liveUser.getLiveStreamingId() === " + liveUser.getLiveStreamingId());
                jsonObject.addProperty("agoraId", seatItem.getAgoraUid());
                int nextMute = (seatItem.isMute() == 1 || seatItem.isMute() == 2) ? 0 : 2;
                jsonObject.addProperty("mute", nextMute);
                jsonObject.addProperty("mutedUserId", seatItem.getUserId());
                if (rtcEngine() != null) {
                    rtcEngine().muteRemoteAudioStream(seatItem.getAgoraUid(), true);
                }
                seatItem.setMute(nextMute);
                seatAdapter.updateData(liveUser.getSeat());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_MUTE_SEAT, jsonObject);
            }

            @Override
            public void onCancelClick() {

            }

            @Override
            public void onClickRemove() {
                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, i);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("userId", seatItem.getUserId());

                MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);
                clearSeatAt(i);

            }
        });
    }

    public void onclickGiftIcon(View view) {
        if (!emojiBottomsheetFragment.isAdded()) {
            giftViewModel.users.clear();
            giftViewModel.users.add(new UserSelectableClass(new PkAudioLiveUserRoot.UsersItem.SeatItem(liveUser.getImage(), liveUser.getCountry(), true, "Host", false, liveUser.getAgoraUID(), 0, true, liveUser.getId(), -1, false, liveUser.getLiveUserId())));

            liveUser.getSeat().stream().filter(PkAudioLiveUserRoot.UsersItem.SeatItem::isReserved).map(UserSelectableClass::new).forEach(giftViewModel.users::add);
            emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojifragfmetn");
        }
    }

    /** Ask server for user details (viewer) then show profile sheet */
    private void getUser(String userId, int postion, PkAudioLiveUserRoot.UsersItem.SeatItem seatItem) {
        customDialogClass.show();
        userListPosition = postion;
        viewerListItem = seatItem;
        Log.d(TAG, "getUser: userListPosition ======" + userListPosition);
        Log.d(TAG, "getUser: viewerListItem ======" + viewerListItem);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fromUserId", sessionManager.getUser().getId());
            jsonObject.put("toUserId", userId);
            Log.d(TAG, "getUser:request  " + jsonObject);
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_GET_USER, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
            customDialogClass.dismiss();
        }
    }

    /** Ask server for user details (comment list / header taps)*/
    private void getUser2(String userId) {
        customDialogClass.show();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fromUserId", sessionManager.getUser().getId());
            jsonObject.put("toUserId", userId);
            Log.d(TAG, "getUser:request  " + jsonObject);
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_GET_USER_2, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
            customDialogClass.dismiss();
        }
    }

    /** Host actions on a viewer: mute/unmute (honors self-mute), remove, kick+block, or invite */
    private void doUserTask(GuestProfileRoot.User userData, int position, PkAudioLiveUserRoot.UsersItem.SeatItem seatItem) {

        new BottomSheetViewersUserProfile(this, seatItem, userData, new BottomSheetViewersUserProfile.OnClickListener() {
            @Override
            public void onUnMute(BottomSheetOnlineProfileBinding sheetDialogBinding) {

                int currentMute = seatItem.isMute(); // 0=none, 1=self, 2=host

                if (currentMute == 1) {

                    new PopupBuilder(HostLiveAudioActivity.this)
                            .showPopUpWithVector(R.drawable.vector_info,
                                    getString(R.string.cannot_unmute_user),
                                    getString(R.string.this_user_has_muted_themselves_only_they_can_unmute),
                                    getString(R.string.okay), new PopupBuilder.OnPopupClickListener() {
                                        @Override
                                        public void onClickContinue() {

                                        }
                                    });

                    // Keep UI reflecting "muted"
                    sheetDialogBinding.txtMic.setText(getString(R.string.unmute_mic));
                    Glide.with(HostLiveAudioActivity.this)
                            .load(R.drawable.speaker_off)
                            .into(sheetDialogBinding.mute);
                    return;
                }

                // Build payload
                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, position);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("liveUserId", liveUser.getLiveUserId());
                jsonObject.addProperty("agoraId", seatItem.getAgoraUid());
                jsonObject.addProperty("mutedUserId", seatItem.getUserId());

                if (currentMute == 2) {
                    // Host-muted → host may unmute (set to 0)
                    jsonObject.addProperty("mute", 0);
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_MUTE_SEAT, jsonObject);

                    seatItem.setMute(0); // update local state if you have a setter
                    sheetDialogBinding.txtMic.setText(getString(R.string.mute_mic));
                    Glide.with(HostLiveAudioActivity.this)
                            .load(R.drawable.speaker)
                            .into(sheetDialogBinding.mute);

                } else { // currentMute == 0 (unmuted) → host mutes (set to 2)
                    jsonObject.addProperty("mute", 2);
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_MUTE_SEAT, jsonObject);

                    seatItem.setMute(2);
                    sheetDialogBinding.txtMic.setText(getString(R.string.unmute_mic));
                    Glide.with(HostLiveAudioActivity.this)
                            .load(R.drawable.speaker_off)
                            .into(sheetDialogBinding.mute);
                }
            }


            @Override
            public void onRemoveSeat() {
                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, position);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("userId", seatItem.getUserId());
                jsonObject.addProperty("name", sessionManager.getUser().getName());
                jsonObject.addProperty("country", sessionManager.getUser().getCountry());
                jsonObject.addProperty("agoraUid", liveUser.getAgoraUID());
                jsonObject.addProperty("removedUserID", seatItem.getUserId());
                jsonObject.addProperty("image", sessionManager.getUser().getImage());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);

            }

            @Override
            public void onKickOut() {

                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, position);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("userId", seatItem.getUserId());
                jsonObject.addProperty("removedUserID", seatItem.getUserId());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);
                blockedUsersList.put(seatItem.getUserId());
                Log.d(TAG, "initLister: blocked " + blockedUsersList.toString());
                try {
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("blocked", blockedUsersList);
                    jsonObject1.put("type", "block");
                    putLiveRoomKeys(jsonObject1);
                    jsonObject1.put("blockedUserId", seatItem.getUserId());
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_UPDATE_BLOCKED_LIST, jsonObject1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void inviteUser() {
                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, position);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("userId", userData.getUserId());
                jsonObject.addProperty("name", userData.getName());
                jsonObject.addProperty("country", userData.getCountry());
                jsonObject.addProperty("agoraUid", -1);
                jsonObject.addProperty("mute", 0);
                jsonObject.addProperty("request", false);
                jsonObject.addProperty("image", userData.getImage());
                jsonObject.addProperty("avatarFrame", userData.getAvatarFrameImage());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_ADD_REQUESTED, jsonObject);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }

    @Override
    public void onErr(int err) {
        Log.d(TAG, "onErr: " + err);
    }

    @Override
    public void onConnectionLost() {
        Log.d(TAG, "onConnectionLost: ");
    }

    @Override
    public void onVideoStopped() {
        Log.d(TAG, "onVideoStopped: ");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sessionManager.getUser().isHost()) {
            hostAPICall.stopApiCallLoop();
        }
        endLive();
        AudioMixingController.getInstance().stop();
        MySocketManager.getInstance().removeAudioRoomHandler(audioRoomHandler);
        MySocketManager.getInstance().removeSocketConnectHandler(socketConnectHandler);
        seatChangeHandler.removeCallbacks(clearSeatChangePending);
        statsManager().clearAllData();
        BaseActivity.STATUS_LIVE = false;
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        Log.d(TAG, "onLeaveChannel: stts " + stats);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.d(TAG, "onJoinChannelSuccess: chanel " + channel + " uid" + uid + "  elapsed " + elapsed);
        runOnUiThread(() -> {
            this.uuid = uid;
            configureAudioRouting(rtcEngine());
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        Log.d(TAG, "onUserOffline: " + uid + " reason" + reason);

    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        Log.d(TAG, "onUserJoined: " + uid + "  elapsed" + elapsed);
    }

    @Override
    public void onLastmileQuality(int quality) {
        Log.d(TAG, "onLastmileQuality: ");
    }

    @Override
    public void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result) {
        Log.d(TAG, "onLastmileProbeResult: ");
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        Log.d(TAG, "onLocalVideoStats: ");
        if (!statsManager().isEnabled()) return;

    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        runOnUiThread(() -> {
            if (rtcStatsView != null && rtcStatsView.getVisibility() == View.VISIBLE) {
                rtcStatsView.setLocalStats(stats.rxKBitRate, stats.rxPacketLossRate, stats.txKBitRate, stats.txPacketLossRate);
            }
        });

    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    /** Audio volume callbacks from Agora: animate host “speaking” ring; forward seats’ VAD to adapter */
    @Override
    public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {

        Log.d(TAG, "onAudioVolumeIndication: speakers.length  " + speakers.length);
        Log.d(TAG, "onAudioVolumeIndication: totalVolume  " + totalVolume);
        Log.d(TAG, "onAudioVolumeIndication: host.getAgoraUID()  " + liveUser.getAgoraUID());
        runOnUiThread(() -> {

            if (totalVolume <= 0) return;
            for (IRtcEngineEventHandler.AudioVolumeInfo info : speakers) {
                Log.d(TAG, "onAudioVolumeIndication:loop " + info.uid);
                Log.d(TAG, "onAudioVolumeIndication:loop " + info.volume);
                if (info.uid == 0) {
                    info.uid = liveUser.getAgoraUID();
                    //   info.channelId = liveUser.getChannel();
                }
            }

            seatAdapter.onAudioVolumeIndication(speakers);

        });

    }

    @Override
    public void onActiveSpeaker(int uid) {
        Log.d(TAG, "onActiveSpeaker: " + uid);
    }

    @Override
    public void onAudioMixingStateChanged(int state, int reason) {
    }

    @Override
    public void onTokenPrivilegeWillExpire(String token) {
        Log.d(TAG, "onTokenPrivilegeWillExpire: ");
        try {
            if (rtcEngine() != null) {
                String tkn = RtcTokenBuilderSample.main(liveUser.getChannel() + "audio", sessionManager.getSetting().getAgoraKey(), sessionManager.getSetting().getAgoraCertificate());
                rtcEngine().renewToken(tkn);
            }
        } catch (Exception e) {
            Log.e(TAG, "onTokenPrivilegeWillExpire: ", e);
        }

    }

    @Override
    public void onRequestToken() {
        Log.d(TAG, "onRequestToken: ");
    }

    @Override
    public void onAudioRouteChanged(int routing) {

    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {

        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void onChannelMediaRelayStateChanged(int state, int code) {

    }

    @Override
    public void onChannelMediaRelayEvent(int code) {

    }

    @Override
    public void onFirstLocalAudioFramePublished(int elapsed) {
        Log.d(TAG, "onFirstLocalAudioFramePublished: " + elapsed);
    }

    @Override
    public void onFirstRemoteAudioFrame(int uid, int elapsed) {
        Log.d(TAG, "onFirstRemoteAudioFrame: " + uid);
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        Log.d(TAG, "onUserMuteAudio: " + uid);
    }

    /** Legacy picker result → sets preview and reuses modern handler to upload */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {

            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

            Glide.with(this).load(data.getData()).apply(requestOptions).into(binding.imgProfile);
            String[] filePathColumn = {DATA};
            Cursor cursor = this.getContentResolver().query(data.getData(), filePathColumn, null, null, null);

            cursor.close();
            Uri selected = data.getData(); // or however you read it above
            handleImageSelection(selected);
        }else if (requestCode == 100 && resultCode == RESULT_OK) {
            ArrayList<AudioDetails> selectedSongs = data.getParcelableArrayListExtra("selectedSong");
            ArrayList<AudioDetails> savedSongs = data.getParcelableArrayListExtra("savedSong");

            ArrayList<AudioDetails> previoussong = loadConfirmedSongs();

            if (savedSongs != null) {
                // Merge new songs with existing saved songs
                for (AudioDetails newSong : savedSongs) {
                    if (!savedSongs.contains(newSong)) { // Prevent duplicates
                        savedSongs.add(newSong);
                    }
                }
                saveConfirmedSongs(previoussong); // Save updated list
            }

            Log.d(TAG, "onActivityResult: ========" + savedSongs.size());
            if (selectedSongs != null && !selectedSongs.isEmpty()) {
                popupBuilder = new PopupBuilder(HostLiveAudioActivity.this);
                popupBuilder.playMusicPopup(selectedSongs, rtcEngine(), new PopupBuilder.OnAddMusicListener() {
                    @Override
                    public void onAddMusicClick() {
                        Intent intent = new Intent(HostLiveAudioActivity.this, AddMusicActivity.class);
                        startActivityForResult(intent, REQUEST_ADD_MUSIC);
                    }
                });
            } else {

            }
        }
    }


    private void saveConfirmedSongs(ArrayList<AudioDetails> songs) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(songs);
        editor.putString(KEY_CONFIRMED_SONGS, json);
        editor.apply();
    }
    private PkAudioLiveUserRoot.UsersItem.SeatItem getSelfPositionFromSeat() {
        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seatList = seatAdapter.getList();
        if (sessionManager.getUser() == null) {
            return null;
        }
        for (PkAudioLiveUserRoot.UsersItem.SeatItem seatItem : seatList) {
            if (seatItem.isReserved() && seatItem.getUserId() != null && seatItem.getUserId().equals(sessionManager.getUser().getId())) {
                return seatItem;
            }
        }
        return null;
    }

    private PkAudioLiveUserRoot.UsersItem.SeatItem getSeatFromPayload(JSONObject payload) {
        if (liveUser == null || liveUser.getSeat() == null) {
            return null;
        }
        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seats = liveUser.getSeat();
        int position = seatIndexFromPayload(payload, seats);
        if (position >= 0 && position < seats.size()) {
            return seats.get(position);
        }
        return null;
    }

    private void applySeatMuteFromPayload(JSONObject payload) {
        PkAudioLiveUserRoot.UsersItem.SeatItem seat = getSeatFromPayload(payload);
        if (seat == null) return;
        seat.setMute(payload.optInt("mute", 0));
        seatAdapter.updateData(liveUser.getSeat());
    }

    private void applySeatLockFromPayload(JSONObject payload) {
        PkAudioLiveUserRoot.UsersItem.SeatItem seat = getSeatFromPayload(payload);
        if (seat == null) return;
        seat.setLock(payload.optBoolean("lock", true));
        seatAdapter.updateData(liveUser.getSeat());
    }

    private void applySeatParticipantFromPayload(JSONObject payload) {
        if (liveUser == null || liveUser.getSeat() == null || payload == null) return;
        int position = seatIndexFromPayload(payload, liveUser.getSeat());
        if (position < 0 || position >= liveUser.getSeat().size()) return;

        String userId = payload.optString("userId", "");
        for (int i = 0; i < liveUser.getSeat().size(); i++) {
            PkAudioLiveUserRoot.UsersItem.SeatItem item = liveUser.getSeat().get(i);
            if (i != position && userId.equals(item.getUserId())) {
                clearSeatItem(item);
            }
        }

        PkAudioLiveUserRoot.UsersItem.SeatItem seat = liveUser.getSeat().get(position);
        seat.setReserved(true);
        seat.setName(payload.optString("name", ""));
        seat.setImage(payload.optString("image", ""));
        seat.setAvatarFrame(payload.optString("avatarFrame", payload.optString("avatarFrameImage", "")));
        seat.setCountry(payload.optString("country", ""));
        seat.setAgoraUid(payload.optInt("agoraUid", 0));
        seat.setMute(payload.optInt("mute", 0));
        seat.setUserId(userId);
        seat.setSpeaking(false);
        bookedSeatItemList = liveUser.getSeat();
        seatAdapter.updateData(liveUser.getSeat());
        finishSeatChange();
    }

    private void clearSeatFromPayload(JSONObject payload) {
        int position = seatIndexFromPayload(payload, liveUser != null ? liveUser.getSeat() : null);
        clearSeatAt(position);
    }

    private void clearSeatAt(int position) {
        if (liveUser == null || liveUser.getSeat() == null || position < 0 || position >= liveUser.getSeat().size()) {
            return;
        }
        PkAudioLiveUserRoot.UsersItem.SeatItem seat = liveUser.getSeat().get(position);
        clearSeatItem(seat);
        seatAdapter.updateData(liveUser.getSeat());
        finishSeatChange();
    }

    private void clearSeatItem(PkAudioLiveUserRoot.UsersItem.SeatItem seat) {
        if (seat == null) return;
        seat.setReserved(false);
        seat.setName("");
        seat.setImage("");
        seat.setAvatarFrame("");
        seat.setCountry("");
        seat.setAgoraUid(0);
        seat.setMute(0);
        seat.setUserId("");
        seat.setSpeaking(false);
    }

    private void reserveSeatForCurrentUser(PkAudioLiveUserRoot.UsersItem.SeatItem seat, int mute, int agoraUid) {
        if (seat == null || sessionManager == null || sessionManager.getUser() == null || liveUser == null || liveUser.getSeat() == null) {
            return;
        }
        seat.setReserved(true);
        seat.setName(sessionManager.getUser().getName());
        seat.setImage(sessionManager.getUser().getImage());
        seat.setAvatarFrame(sessionManager.getUser().getAvatarFrameImage());
        seat.setCountry(sessionManager.getUser().getCountry());
        seat.setAgoraUid(agoraUid);
        seat.setMute(mute);
        seat.setUserId(sessionManager.getUser().getId());
        seat.setSpeaking(false);
        seatAdapter.updateData(liveUser.getSeat());
    }

    private void muteAllGuestSeats() {
        if (liveUser == null || liveUser.getSeat() == null || liveUser.getSeat().isEmpty()) {
            Toast.makeText(this, "No microphones to mute", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : "";
        int affectedSeats = 0;

        for (int i = 0; i < liveUser.getSeat().size(); i++) {
            PkAudioLiveUserRoot.UsersItem.SeatItem seatItem = liveUser.getSeat().get(i);
            if (!seatItem.isReserved()
                    || TextUtils.isEmpty(seatItem.getUserId())
                    || seatItem.getUserId().equals(currentUserId)
                    || seatItem.isMute() == 2) {
                continue;
            }

            JsonObject jsonObject = new JsonObject();
            putSeatIndex(jsonObject, i);
            putLiveRoomKeys(jsonObject);
            jsonObject.addProperty("liveUserId", liveUser.getLiveUserId());
            jsonObject.addProperty("agoraId", seatItem.getAgoraUid());
            jsonObject.addProperty("mute", 2);
            jsonObject.addProperty("mutedUserId", seatItem.getUserId());

            if (rtcEngine() != null && seatItem.getAgoraUid() > 0) {
                rtcEngine().muteRemoteAudioStream(seatItem.getAgoraUid(), true);
            }

            seatItem.setMute(2);
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_MUTE_SEAT, jsonObject);
            affectedSeats++;
        }

        if (affectedSeats > 0) {
            seatAdapter.updateData(liveUser.getSeat());
            Toast.makeText(this, "All guest microphones muted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No guest microphones to mute", Toast.LENGTH_SHORT).show();
        }
    }


    private void becomeHost(PkAudioLiveUserRoot.UsersItem.SeatItem seatItem, boolean fromOnSeatClicked) {
        if (rtcEngine() != null) {
            rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            rtcEngine().enableAudio();
            rtcEngine().disableVideo();
            rtcEngine().muteLocalAudioStream(viewModel.isMuted);

            // keep exactly the same output path & playback volume as audience
            rtcEngine().adjustRecordingSignalVolume(viewModel.isMuted ? 0 : 100);
            ensureSpeakerphone();
            normalizePlaybackVolume();
        }
    }

    private boolean hasExternalAudioRoute(AudioManager am) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (AudioDeviceInfo dev : am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                int t = dev.getType();
                if (t == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                        || t == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || t == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || t == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
                    return true;
                }
            }
            return false;
        } else {
            // Legacy best-effort
            return am.isBluetoothScoOn() || am.isBluetoothA2dpOn() || am.isWiredHeadsetOn();
        }
    }

    private void ensureSpeakerphone() {
        if (rtcEngine() != null) rtcEngine().setEnableSpeakerphone(true);
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;

        am.setMode(AudioManager.MODE_IN_COMMUNICATION);

        if (!hasExternalAudioRoute(am)) {
            if (Build.VERSION.SDK_INT >= 31) {
                // Explicitly pick the built-in speaker for voice communication
                for (AudioDeviceInfo dev : am.getAvailableCommunicationDevices()) {
                    if (dev.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                        am.setCommunicationDevice(dev);
                        break;
                    }
                }
            } else {
                // Pre-S / fallback
                am.setSpeakerphoneOn(true);
            }
        }
    }

    private void normalizePlaybackVolume() {
        if (rtcEngine() != null) {
            // 100 = default. Use 120–200 if you want a bit louder playback for all remotes.
            rtcEngine().adjustPlaybackSignalVolume(250);
        }
    }

    private void checkAndProceed() {
        confirmedSongs = loadConfirmedSongs();

        if (confirmedSongs != null && !confirmedSongs.isEmpty()) {
            popupBuilder = new PopupBuilder(HostLiveAudioActivity.this);
            popupBuilder.playMusicPopup(confirmedSongs, rtcEngine(), new PopupBuilder.OnAddMusicListener() {
                @Override
                public void onAddMusicClick() {
                    Intent intent = new Intent(HostLiveAudioActivity.this, AddMusicActivity.class);
                    startActivityForResult(intent, REQUEST_ADD_MUSIC);
                }
            }); // Pass rtcEngine() if needed
        } else {
            // Otherwise, launch AddMusicActivity
            Intent intent = new Intent(HostLiveAudioActivity.this, AddMusicActivity.class);
            startActivityForResult(intent, REQUEST_ADD_MUSIC);
        }
    }

    private ArrayList<AudioDetails> loadConfirmedSongs() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        String json = sharedPreferences.getString(KEY_CONFIRMED_SONGS, null);
        Log.d("TAG", "loadConfirmedSongs: " + json);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<AudioDetails>>() {
            }.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: isFinishing ===== " + isFinishing());
    }

    @Override
    protected void onResume() {
        super.onResume();
        configureAudioRouting(rtcEngine());
        if (sessionManager.getUser().isHost()) {
            hostAPICall.startApiCallLoop();
        }
    }
}
