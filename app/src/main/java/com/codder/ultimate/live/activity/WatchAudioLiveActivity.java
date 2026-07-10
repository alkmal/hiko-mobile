package com.codder.ultimate.live.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
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
import com.codder.ultimate.agora.stats.RemoteStatsData;
import com.codder.ultimate.agora.stats.StatsData;
import com.codder.ultimate.agora.token.RtcTokenBuilderSample;
import com.codder.ultimate.bottomsheets.BottomSheetReport;
import com.codder.ultimate.bottomsheets.BottomsheetGuestUserProfile;
import com.codder.ultimate.databinding.ActivityWatchAudioLiveBinding;
import com.codder.ultimate.databinding.ItemSeatBinding;
import com.codder.ultimate.live.adapter.GiftReceiveAdapter;
import com.codder.ultimate.live.adapter.SeatAdapter;
import com.codder.ultimate.live.bottomsheet.BottomSheetAudioRoomPasscode;
import com.codder.ultimate.live.bottomsheet.BottomSheetGameCasino;
import com.codder.ultimate.live.bottomsheet.BottomSheetGameList;
import com.codder.ultimate.live.bottomsheet.BottomSheetGameTeenPatti;
import com.codder.ultimate.live.bottomsheet.BottomSheetReactions;
import com.codder.ultimate.live.bottomsheet.DialogGame;
import com.codder.ultimate.live.bottomsheet.UserProfileBottomSheet;
import com.codder.ultimate.live.fragment.EmojiBottomSheetFragment;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.model.LiveStramComment;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.live.model.ReactionsViewModel;
import com.codder.ultimate.live.utils.FloatingButtonService;
import com.codder.ultimate.live.utils.UserSelectableClass;
import com.codder.ultimate.live.viewModel.EmojiSheetViewModel;
import com.codder.ultimate.modelclass.BroadcastBannerRoot;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.modelclass.WatchLiveViewModel;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.socket.AudioRoomHandler;
import com.codder.ultimate.socket.MySocketManager;
import com.codder.ultimate.socket.SocketConnectHandler;
import com.codder.ultimate.utils.ImageUrlUtil;
import com.codder.ultimate.utils.SvgaCacheManager;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.gson.Gson;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;

import eightbitlab.com.blurview.RenderEffectBlur;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;

public class WatchAudioLiveActivity extends AgoraBaseActivity {

    private static final String TAG = "WatchLiveActivity";
    private static int MY_UID = 0;
    ActivityWatchAudioLiveBinding binding;
    boolean isMuteByHost = false;
    SessionManager sessionManager;
    String token = "";
    EmojiBottomSheetFragment emojiBottomsheetFragment;
    List<PkAudioLiveUserRoot.UsersItem.SeatItem> bookedSeatItemList = new ArrayList<>();
    WatchLiveViewModel viewModel;
    PkAudioLiveUserRoot.UsersItem host;
    EmojiSheetViewModel giftViewModel;
    int uuid;
    ArrayList<PkAudioLiveUserRoot.UsersItem.SeatItem> coHostList = new ArrayList<>();
    SeatAdapter seatAdapter;
    int selfPosition = -1;
    UserProfileBottomSheet userProfileBottomSheet;
    GridLayoutManager gridLayoutManager;
    boolean isHost = false;
    JSONArray blockedUsersList = new JSONArray();
    long animationDurationMillis;
    List<GiftRoot.GiftItem> giftList = new ArrayList<>();
    GiftReceiveAdapter giftReceiveAdapter;

    private boolean hostExists;

    private Handler reactionHandler = new Handler();
    private Runnable currentReactionRunnable;
    private boolean isReactionRunning = false;

    private BottomSheetReactions bottomSheetReactions;
    private boolean isUserJoined = false;
    private String lastLocalCommentText = "";
    private long lastLocalCommentAt = 0L;
    private boolean seatChangePending = false;
    private final Handler seatChangeHandler = new Handler(Looper.getMainLooper());
    private final Runnable clearSeatChangePending = () -> seatChangePending = false;

    private boolean isOptionsExpanded = false;


    private void setupOptionsToggle() {
        List<View> iconViews = Arrays.asList(
                binding.btnReaction,
                binding.btnMute,
                binding.imgGame
        );


        binding.imgOption.setOnClickListener(v -> {
            if (!isOptionsExpanded) {
                // Expand — એક પછી એક ઉપર animate કરો
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
                // "Options ^" label update
                // (optional: tvOptionsLabel text change)
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

    AudioRoomHandler audioRoomHandler = new AudioRoomHandler() {
        @Override
        public void onBroadcastNotification(Object[] args) {
            Log.d(TAG, "onBroadcastNotification: ======" + args[0].toString());
            handleNotification(args, "onBrodcastNotification");
        }

        /** Update sticky "announcement" row when host changes welcome text (no new comment row)*/
        @Override
        public void onRoomWelcome(Object[] args) {
            runOnUiThread(() -> {
                if (args == null || args.length == 0 || args[0] == null) return;

                try {
                    org.json.JSONObject data = new org.json.JSONObject(args[0].toString());
                    String liveId = data.optString("liveStreamingId", "");
                    String msg = data.optString("roomWelcome", "");

                    if (host != null && liveId.equals(host.getLiveStreamingId())) {
                        host.setRoomWelcome(msg);

                        if (viewModel != null && viewModel.liveStramCommentAdapter != null) {
                            String prefix = getString(R.string.announcement); // e.g., "Announcement: "
                            String text = !msg.isEmpty()
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
                    throw new RuntimeException(e);
                }
            });
        }

        /**
         * Adds the notification to the queue and starts processing if not already animating.
         */
        private void enqueueNotification(JSONObject jsonObject, String eventType) {
            try {
                // Add the event type for distinguishing between onGame and onBroadcastNotification
                jsonObject.put("eventType", eventType);
            } catch (JSONException e) {
                Log.e(TAG, "enqueueNotification: ", e);
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
                String eventType = null;
                if (jsonObject != null) {
                    eventType = jsonObject.getString("eventType");
                }
                String name;
                String imageUrl;
                String giftUrl;

                if ("ongame".equals(eventType)) {
                    // Handle onGame JSON structure
                    name = jsonObject.getString("message");
                    imageUrl = jsonObject.getString("userImage");
                    binding.icGiftImage.setVisibility(GONE);
                    // Load user image
                    Glide.with(WatchAudioLiveActivity.this)
                            .load(imageUrl)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .into(binding.ivGameUserImage);

                    binding.tvGameNotification.setText(name);


                    // Load random banner image if available
                    List<BroadcastBannerRoot.BroadcastBannerItem> bannerItemList = sessionManager.getGameBroadcastBannerList();
                    if (bannerItemList != null && !bannerItemList.isEmpty()) {
                        String randomBannerUrl = getRandomBannerImage(bannerItemList);

                        Glide.with(WatchAudioLiveActivity.this)
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
                    Glide.with(WatchAudioLiveActivity.this)
                            .load(imageUrl)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .into(binding.ivUserImage);

                    binding.tvNotification.setText(name);

                    Glide.with(WatchAudioLiveActivity.this)
                            .load(giftUrl)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .into(binding.icGiftImage);

                    // Load random banner image if available
                    List<BroadcastBannerRoot.BroadcastBannerItem> bannerItemList = sessionManager.getBroadcastBannerList();
                    if (bannerItemList != null && !bannerItemList.isEmpty()) {
                        String randomBannerUrl = getRandomBannerImage(bannerItemList);

                        Glide.with(WatchAudioLiveActivity.this)
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
                throw new RuntimeException(e);
            }
        }

        private void startBannerAnimation() {
            binding.lytNotification.setVisibility(View.VISIBLE);

            Animation slideIn = AnimationUtils.loadAnimation(WatchAudioLiveActivity.this, R.anim.anim_slide_right_to_left);
            binding.lytNotification.startAnimation(slideIn);

            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Animation slideOut = AnimationUtils.loadAnimation(WatchAudioLiveActivity.this, R.anim.slide_right_to_left);
                        binding.lytNotification.startAnimation(slideOut);

                        slideOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                binding.lytNotification.setVisibility(View.GONE);
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

            Animation slideIn = AnimationUtils.loadAnimation(WatchAudioLiveActivity.this, R.anim.anim_slide_right_to_left);
            binding.lytGameNotification.startAnimation(slideIn);

            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Animation slideOut = AnimationUtils.loadAnimation(WatchAudioLiveActivity.this, R.anim.slide_right_to_left);
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
            runOnUiThread(() -> {
                Log.d(TAG, "onTotalRoomCoins: ==>  " + args[0].toString());
                binding.tvRcoins.setText(args[0].toString());
            });
        }

        /** Host present/absent UI: toggles placeholder vs. framed avatar based on isHostExists*/
        @Override
        public void onAudioLiveHostRemove(Object[] args) {
            runOnUiThread(() -> {

                if (args[0] != null) {
                    Log.d(TAG, "onAudioLiveHostRemove: ==>" + args[0].toString());
                    String data = args[0].toString();
                    Log.d(TAG, "onAudioLiveHostRemove: " + data);

                    if (!data.isEmpty()) {
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            hostExists = jsonObject.getBoolean("isHostExists");

                            if (hostExists) {
                                binding.mainHostProfileImage.setUserImage(host.getImage(), host.getAvatarFrameImage(), 20);
                                binding.mainHostProfileImage.setVisibility(VISIBLE);
                                binding.ivPlaceholder.setVisibility(GONE);
                            } else {
                                binding.ivPlaceholder.setVisibility(VISIBLE);
                                binding.mainHostProfileImage.setVisibility(GONE);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "onAudioLiveHostRemove: ", e);
                        }
                    }
                }
            });
        }

        @Override
        public void onHostEnter(Object[] args) {
            runOnUiThread(() -> Log.d(TAG, "run: ==>enter host"));
        }

        @Override
        public void onLiveEndByEnd(Object[] args) {

        }

        @Override
        public void onComment(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {
                    String data = args[0].toString();
                    Log.d(TAG, "onComment: " + data);

                    if (!data.isEmpty()) {
                        LiveStramComment liveStramComment = new Gson().fromJson(data.toString(), LiveStramComment.class);
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
        public void onUserCoinUpdate(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {
                    UserRoot.User user = sessionManager.getUser();
                    user.setDiamond(Integer.parseInt(args[0].toString()));
                    sessionManager.saveUser(user);
                });
            }
        }

        private Queue<JSONObject> giftQueue = new LinkedList<>();
        private boolean isGiftDisplaying = false;
        long timeStamp;

        @Override
        public void onGift(Object[] args) {

            runOnUiThread(() -> {
                if (args.length > 0 && args[0] != null) {
                    String data = args[0].toString();
                    Log.d(TAG, "onGift0: " + args[0].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        if (jsonObject.get("gift") != null) {
                            // Add the gift data to the queue

                            giftQueue.add(jsonObject);

                            if (!isGiftDisplaying) {
                                processNextGift();
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "joinChannel: ", e);
                    }
                }

                if (args.length > 1 && args[1] != null) {  // gift sender user
                    Log.d(TAG, "onGift1:  " + args[1].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[1].toString());
                        UserRoot.User user = new Gson().fromJson(jsonObject.toString(), UserRoot.User.class);
                        if (user != null) {
                            if (user.getId().equals(sessionManager.getUser().getId())) {
                                sessionManager.saveUser(user);
                                giftViewModel.localUserCoin.setValue(user.getDiamond());
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "joinChannel: ", e);
                    }

                }

                if (args.length > 2 && args[2] != null) {   // host
                    Log.d(TAG, "onGift2:  " + args[2].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[2].toString());
                        UserRoot.User user = new Gson().fromJson(jsonObject.toString(), UserRoot.User.class);
                        if (user != null && user.getId().equals(host.getLiveUserId())) {
                            Log.d(TAG, "onGift2: got host    " + user.toString());
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "joinChannel: ", e);
                    }
                }
            });

        }

        /** Gift intake → dedupe by timestamp → render image/SVGA → chain next gift from queue*/
        private void processNextGift() {
            if (!giftQueue.isEmpty()) {
                isGiftDisplaying = true;
                JSONObject giftJson = giftQueue.poll(); // Get the next gift
                try {
                    long receivedTimeStamp = giftJson.getLong("timeStamp");

                    if (timeStamp != receivedTimeStamp) {
                        timeStamp = receivedTimeStamp;

                        Log.d(TAG, "processNextGift:  ");

                        GiftRoot.GiftItem giftData = new Gson().fromJson(giftJson.get("gift").toString(), GiftRoot.GiftItem.class);
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

                            Log.d(TAG, "processNextGift: giftData  " + giftData);

                            if (giftData.getType() == 2) {
                                if (finalGiftLink.contains(".webp")) {
                                    handleImageGift(finalGiftLink, giftData);
                                } else {
                                    handleSVGAGift(finalGiftLink, giftJson, giftData);
                                }
                            } else {
                                handleImageGift(finalGiftLink, giftData);
                            }


                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "joinChannel: ", e);
                }

                isGiftDisplaying = false;
                if (!giftQueue.isEmpty()) {
                    processNextGift();
                }

            } else {
                isGiftDisplaying = false;
            }
        }

        /** Show simple (webp/png/gif) gift in ribbon and remove after 3s*/
        private void handleImageGift(String giftLink, GiftRoot.GiftItem giftData) {
            Log.d(TAG, "handleImageGift: ==> " + giftData);
            giftList.clear();
            giftList.add(giftData);
            giftReceiveAdapter.addData(giftList);

            new Handler().postDelayed(() -> {
                giftReceiveAdapter.remove(giftData);
                giftList.remove(giftData);
                processNextGift(); // Start the next gift immediately
            }, 3000); // Use the same duration as in your original code
        }

        /** SVGA gift: cache-first, URL fallback; compute exact duration, hide, then process next*/
        private void handleSVGAGift(String giftLink, JSONObject jsonObject, GiftRoot.GiftItem giftData) {

            binding.svgaImage.setVisibility(View.VISIBLE);
            SVGAImageView imageView = binding.svgaImage;

            SvgaCacheManager.decodeSvgaFromCache(WatchAudioLiveActivity.this, giftLink, new SVGAParser.ParseCompletion() {
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
                        binding.lytSvgagift.setVisibility(View.GONE);
                        imageView.clear();
                        imageView.setVisibility(View.GONE);
                        processNextGift();
                    }, duration);
                }

                @Override
                public void onError() {
                    Log.w(TAG, "⚠️ SVGA not found in cache, falling back to URL: " + giftLink);
                    try {
                        SVGAParser parser = new SVGAParser(WatchAudioLiveActivity.this);
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
                                    binding.lytSvgagift.setVisibility(View.GONE);
                                    imageView.clear();
                                    imageView.setVisibility(View.GONE);
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
                        Log.e(TAG, "joinChannel: ", e);
                        processNextGift();
                    }
                }
            });
        }

        private Queue<JSONObject> entryQueue = new LinkedList<>();
        private boolean isEntryEffectRunning = false;

        @Override
        public void onView(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Object args1 = args[0];
                    Log.d(TAG, "viewListener : " + args1.toString());

                    try {
                        JSONArray jsonArray = new JSONArray(args1.toString());
                        JSONArray finalArray = new JSONArray();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject.getBoolean("isAdd")) {
                                finalArray.put(jsonObject);
                            }
                        }

                        List<JSONObject> userList = new ArrayList<>();
                        for (int i = 0; i < finalArray.length(); i++) {
                            try {
                                userList.add(finalArray.getJSONObject(i));
                            } catch (JSONException e) {
                                Log.e(TAG, "joinChannel: ", e);
                            }
                        }
                        viewModel.liveViewUserAdapter.submitList(userList);
                        binding.tvViewUserCount.setText(String.valueOf(finalArray.length()));
                        Log.d(TAG, "views2 : " + jsonArray);
                    } catch (JSONException e) {
                        Log.d(TAG, "207: ");
                        Log.e(TAG, "joinChannel: ", e);
                    }

                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("blocked", blockedUsersList);
                        putLiveRoomKeys(jsonObject);
                        Log.d(TAG, "onView: data send " + jsonObject);
                        MySocketManager.getInstance().getSocket().emit(Const.EVENT_BLOCK, jsonObject);

                    } catch (JSONException e) {
                        Log.e(TAG, "joinChannel: ", e);
                    }
                }

                if (args.length > 1 && args[1] != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(args[1].toString());
                        if (jsonObject.has("entrySvga") && jsonObject.has("avatarFrame") && jsonObject.has("image")) {
                            Log.d(TAG, "onView: New Entry Detected: " + jsonObject.toString());
                            entryQueue.add(jsonObject);
                            triggerNextEntryEffect();  // Trigger the next effect
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "joinChannel: ", e);
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
                    Glide.with(WatchAudioLiveActivity.this).load(ImageUrlUtil.normalize(userImage)).circleCrop().into(binding.userImage);
                    Glide.with(WatchAudioLiveActivity.this).load(avatarFrame != null && !avatarFrame.isEmpty() ? ImageUrlUtil.normalize(avatarFrame) : "").into(binding.avatarFrameImage);

                    Animation animation = AnimationUtils.loadAnimation(WatchAudioLiveActivity.this, R.anim.slide_in_right);
                    animation.setFillAfter(true);
                    binding.nameLyt.startAnimation(animation);
                } else {
                    binding.layEntry.setVisibility(GONE);
                }

            } catch (JSONException e) {
                Log.e(TAG, "joinChannel: ", e);
                isEntryEffectRunning = false;
                binding.layEntry.setVisibility(GONE);
                binding.svgaImage.clear();
                triggerNextEntryEffect();  // Proceed even if something fails
            }
        }

        private void displaySVGAEntry(String entryLink) {

            binding.svgImage.clear();
            binding.layEntry.setVisibility(View.VISIBLE);
            SVGAImageView imageView = binding.svgImage;

            SvgaCacheManager.decodeSvgaFromCache(WatchAudioLiveActivity.this, BuildConfig.BASE_URL + entryLink, new SVGAParser.ParseCompletion() {
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
                        binding.layEntry.setVisibility(View.GONE);
                        binding.svgImage.setVisibility(View.GONE);
                        imageView.clear();
                        imageView.setVisibility(View.GONE);
                    }, duration);

                }

                @Override
                public void onError() {

                    SVGAParser parser = new SVGAParser(WatchAudioLiveActivity.this);
                    try {
                        parser.decodeFromURL(new URL(entryLink != null && !entryLink.isEmpty() ? BuildConfig.BASE_URL + entryLink : ""), new SVGAParser.ParseCompletion() {
                            @Override
                            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
                                dynamicEntity.setDynamicImage(BuildConfig.BASE_URL + entryLink, "99");
                                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity, dynamicEntity);
                                Log.d(TAG, "onComplete: from server==>" + BuildConfig.BASE_URL + entryLink);
                                imageView.setImageDrawable(drawable);
                                imageView.startAnimation();

                                animationDurationMillis = svgaVideoEntity.getFrames() / svgaVideoEntity.getFPS() * 1000L;

                                new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                                    binding.svgImage.setVisibility(View.GONE);
                                    binding.layEntry.setVisibility(View.GONE);
                                    binding.svgImage.clear();
                                }, animationDurationMillis);
                            }

                            @Override
                            public void onError() {

                            }
                        }, null);
                    } catch (MalformedURLException e) {
                        Log.e(TAG, "joinChannel: ", e);
                    }
                }
            });
        }

        @Override
        public void onAddRequested(Object[] args) {

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
                        int position = seatIndexFromPayload(payload, currentSeatList());
                        clearSeatAt(position);
                        String removedUserId = payload.optString("userId", "");
                        boolean removedMe = sessionManager.getUser() != null && removedUserId.equals(sessionManager.getUser().getId());
                        if ((position == selfPosition || removedMe) && rtcEngine() != null) {
                            forceAudienceListenOnly();
                            selfPosition = -1;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "onLessParticipants: ", e);
                    }
                });
            }
        }

        @Override
        public void onMuteSeat(Object[] args) {
            Log.d(TAG, "onMuteSeat: " + args[0].toString());
            if (args[0] != null) {
                runOnUiThread(() -> {
                    String data = args[0].toString();
                    try {
                        JSONObject json = new JSONObject(data);
                        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seatList = currentSeatList();
                        int seatPosition = seatIndexFromPayload(json, seatList);

                        if (seatPosition != -1) {
                            if (seatPosition >= 0 && seatPosition < seatList.size()) {
                                seatList.get(seatPosition).setMute(json.optInt("mute", 0));
                                bookedSeatItemList = seatList;
                                seatAdapter.updateData(seatList);
                            }
                            if (json.has("agoraId")) {
                                if (seatPosition >= 0 && seatPosition < seatList.size()
                                        && seatList.get(seatPosition).getAgoraUid() == json.getInt("agoraId")) {
                                    int mute = json.getInt("mute");
                                    String seatUserId = seatList.get(seatPosition).getUserId();
                                    String localUserId = sessionManager.getUser().getId();
                                    if (seatUserId != null && seatUserId.equals(localUserId)) {
                                        if (mute == 2) {
                                            isMuteByHost = true;
                                            viewModel.isMuted = true;
                                            binding.btnMute.setEnabled(false);
                                            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.mute_blocked));
                                        } else if (mute == 1) {
                                            isMuteByHost = false;
                                            viewModel.isMuted = true;
                                            binding.btnMute.setEnabled(true);
                                            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_mute));
                                        } else {
                                            isMuteByHost = false;
                                            viewModel.isMuted = false;
                                            binding.btnMute.setEnabled(true);
                                            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_unmute));
                                        }
                                        if (rtcEngine() != null) {
                                            rtcEngine().muteLocalAudioStream(viewModel.isMuted);
                                            rtcEngine().adjustRecordingSignalVolume(viewModel.isMuted ? 0 : 100);
                                        }
                                        Log.d(TAG, "onMuteSeat: isMute isMuteByHost==>= " + isMuteByHost);
                                        Log.d(TAG, "onMuteSeat: isMute viewModel.isMuted==> " + viewModel.isMuted);
                                    }
                                }
                            }
                        } else {
                            if (json.getInt("mute") == 1 || json.getInt("mute") == 2) {
                                binding.ivMute.setVisibility(VISIBLE);
                            } else {

                                rtcEngine().setEnableSpeakerphone(true);

                                AudioManager audioManager;
                                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                if (audioManager != null) {
                                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                                    audioManager.setSpeakerphoneOn(true);
                                }

                                binding.ivMute.setVisibility(GONE);
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        @Override
        public void onLockSeat(Object[] args) {
            if (args != null && args.length > 0 && args[0] != null) {
                runOnUiThread(() -> {
                    try {
                        JSONObject payload = new JSONObject(args[0].toString());
                        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seatList = currentSeatList();
                        int position = seatIndexFromPayload(payload, seatList);
                        if (position >= 0 && position < seatList.size()) {
                            seatList.get(position).setLock(payload.optBoolean("lock", true));
                            bookedSeatItemList = seatList;
                            seatAdapter.updateData(seatList);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "onLockSeat: ", e);
                    }
                });
            }
        }

        /** Background/theme change with disk cache; keeps default placeholder as fallback*/
        @Override
        public void onChangeTheme(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Log.d(TAG, "call: onChangeTheme  " + args[0]);
                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        String image = jsonObject.getString("background");
                        host.setBackground(image);
                        Glide.with(WatchAudioLiveActivity.this)
                                .load(ImageUrlUtil.normalize(image))
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.app_main_bg)
                                .into(binding.mainImg);
                    } catch (JSONException e) {
                        Log.e(TAG, "onChangeTheme: ", e);
                    }
                }
            });
        }

        /** Seat payload → UI sync: span count, rCoins, co-host list; auto-drop self if seat shrinks*/
        @Override
        public void onSeat(Object[] args) {
            if (args[0] != null) {
                Log.d(TAG, "onSeat: listen ==> " + args[0].toString());
                Log.d("onAudioVolumeIndication", ": seat listener" + args[0]);

                runOnUiThread(() -> {
                    String data = args[0].toString();
                    Log.d("onAudioVolumeIndication", "initLister: usr sty1 " + data);
                    JsonParser parser = new JsonParser();
                    JsonElement mJson = parser.parse(data);
                    Log.d("onAudioVolumeIndication", "initLister: usr sty2 " + mJson);
                    Gson gson = new Gson();

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(data);
                        hostExists = jsonObject.getBoolean("isHostExists");

                        JSONObject audioConfig = jsonObject.optJSONObject("audioConfig");
                        int isHostMute = (audioConfig != null) ? audioConfig.optInt("isHostMute", 0) : 0;

                        Log.d("mute==>", "seat: ==>" + isHostMute);

                        if (isHostMute == 1) {
                            binding.ivMute.setVisibility(VISIBLE);
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
                        Log.d(TAG, "run: === watch set isHostMute + " + isHostMute);

                        if (hostExists) {
                            binding.mainHostProfileImage.setUserImage(host.getImage(), host.getAvatarFrameImage(), 20);
                            binding.mainHostProfileImage.setVisibility(VISIBLE);
                            binding.ivPlaceholder.setVisibility(GONE);
                        } else {
                            binding.ivPlaceholder.setVisibility(VISIBLE);
                            binding.mainHostProfileImage.setVisibility(GONE);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    PkAudioLiveUserRoot.UsersItem userData = gson.fromJson(mJson, PkAudioLiveUserRoot.UsersItem.class);
                    host.setSeat(userData.getSeat());
                    binding.tvRcoins.setText(RayziUtils.formatCoin(userData.getRCoin()));
                    bookedSeatItemList = userData.getSeat();
                    runOnUiThread(() -> {
                        if (host.getSeat().size() >= 15) {
                            gridLayoutManager.setSpanCount(5); // Change to 5 columns when item count is 16 or more
                        } else {
                            gridLayoutManager.setSpanCount(4); // Default back to 4 columns for less than 16 items
                        }
                        seatAdapter.updateData(userData.getSeat());
                        PkAudioLiveUserRoot.UsersItem.SeatItem selfSeat = getSelfPositionFromSeat();
                        if (selfSeat == null && selfPosition != -1) {
                            forceAudienceListenOnly();
                            selfPosition = -1;
                            binding.btnMute.setEnabled(true);
                            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_mute));
                        } else if (selfSeat != null) {
                            selfPosition = seatIndexForSeat(selfSeat);
                        }
                        finishSeatChange();
                    });
                    coHostList.clear();
                    for (int i = 0; i < userData.getSeat().size(); i++) {
                        if (userData.getSeat().get(i).getUserId() != null) {
                            coHostList.add(userData.getSeat().get(i));
                        }
                    }

                    Log.d(TAG, "doWork: ==> Position " + selfPosition);
                    Log.d(TAG, "doWork: ==> Seat Size " + host.getSeat().size());

                    // If the user's seat position is greater than the updated seat size, remove them from their seat.
                    if (selfPosition >= host.getSeat().size()) {
                        JsonObject jsonObject1 = new JsonObject();
                        putSeatIndex(jsonObject1, selfPosition);
                        putLiveRoomKeys(jsonObject1);
                        jsonObject1.addProperty("userId", sessionManager.getUser().getId());
                        MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject1);
                        Log.d(TAG, "doWork: become audence");
                        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                        rtcEngine().disableVideo();
                        isHost = false;
                        selfPosition = -1;
                    }
                });
            }
        }

        @Override
        public void onBlock(Object[] args) {
            Log.d(TAG, "onBlock: " + args[0].toString());
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Object data = args[0];
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray blockedList = jsonObject.getJSONArray("blocked");
                        Log.d(TAG, "onBlock: blocklist " + blockedList.length());
                        for (int i = 0; i < blockedList.length(); i++) {
                            Log.d(TAG, "block user : " + blockedList.get(i).toString());
                            if (blockedList.get(i).toString().equals(sessionManager.getUser().getId())) {
                                Toast.makeText(WatchAudioLiveActivity.this, getString(R.string.you_are_blocked_by_host), Toast.LENGTH_SHORT).show();
                                new Handler(Looper.myLooper()).postDelayed(() -> confirmEndLive(), 500);
                            }
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "onBlock: ", e);
                    }
                }
            });

        }

        @Override
        public void onBanned(Object[] args) {
            Log.d(TAG, "onBlock:onBanned " + args[0].toString());
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Object data = args[0];
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray blockedList = jsonObject.getJSONArray("blocked");
                        Log.d(TAG, "onBlock: blocklist " + blockedList.length());
                        for (int i = 0; i < blockedList.length(); i++) {
                            String blockedId = blockedList.optString(i, "");

                            Log.d(TAG, "onBanned: " + blockedId.toString());
                            String blockedUserId = jsonObject.optString("blockedUserId", "");
                            String currentUserId = sessionManager.getUser().getId();

                            if (blockedUserId.equals(currentUserId)) {
                                if (binding != null && !isFinishing()) {
                                    new Handler(Looper.myLooper()).postDelayed(() -> confirmEndLive(), 500);
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

        }

        @Override
        public void onBlockUserAlert(Object[] args) {
            Log.d(TAG, "onBlockUserAlert " + args[0].toString());
            runOnUiThread(() -> {
                Toast.makeText(WatchAudioLiveActivity.this, getString(R.string.you_are_blocked_by_host), Toast.LENGTH_SHORT).show();
                confirmEndLive();
            });
        }

        @Override
        public void onGetUser(Object[] args) {
            Log.d(TAG, "onGetUser: " + args[0].toString());
            runOnUiThread(() -> {
                if (args[0] != null) {
                    String data = args[0].toString();
                    JsonParser parser = new JsonParser();
                    JsonElement mJson = parser.parse(data);
                    Gson gson = new Gson();
                    GuestProfileRoot.User userData = gson.fromJson(mJson, GuestProfileRoot.User.class);

                    if (userData != null) {
                        if (userData.getUserId().equals(host.getLiveUserId())) {
                            userProfileBottomSheet.show(false, userData, host.getLiveStreamingId(), true);
                        } else {
                            userProfileBottomSheet.show(false, userData, "", true);
                        }
                    }
                    customDialogClass.dismiss();
                }
            });

        }

        @Override
        public void onGetUser2(Object[] args) {

        }

        @Override
        public void onInvite(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(args[0].toString());

                        Log.d(TAG, "call:inviteListener " + args[0]);
                        String id = jsonObject1.getString("userId");
                        if (id.equalsIgnoreCase(sessionManager.getUser().getId())) {
                            new PopupBuilder(WatchAudioLiveActivity.this).showPkRequestPopup(getString(R.string.audio_request_received_from) + host.getName(), host.getImage(), host.getAvatarFrameImage(), "Accept", "Decline", new PopupBuilder.OnMultiButtonPopupLister() {
                                @Override
                                public void onClickContinue() {
                                    if (!beginSeatChange()) return;
                                    JsonObject jsonObject = new JsonObject();
                                    try {
                                        int invitedPosition = seatIndexFromPayload(jsonObject1, currentSeatList());
                                        if (invitedPosition < 0) {
                                            finishSeatChange();
                                            return;
                                        }
                                        putSeatIndex(jsonObject, invitedPosition);
                                        jsonObject.addProperty("liveUserMongoId", host.getId());
                                        jsonObject.addProperty("userId", sessionManager.getUser().getId());
                                        jsonObject.addProperty("name", sessionManager.getUser().getName());
                                        jsonObject.addProperty("country", sessionManager.getUser().getCountry());
                                        jsonObject.addProperty("agoraUid", MY_UID);
                                        putLiveRoomKeys(jsonObject);
                                        jsonObject.addProperty("image", sessionManager.getUser().getImage());
                                        jsonObject.addProperty("avatarFrame", sessionManager.getUser().getAvatarFrameImage());
                                        int currentState = jsonObject1.optInt("mute", 0);
                                        if (currentState == 0) {
                                            currentState = viewModel.isMuted ? 1 : 0;
                                        }
                                        PkAudioLiveUserRoot.UsersItem.SeatItem selfPos = getSelfPositionFromSeat();
                                        if (selfPos != null && selfPos.isMute() == 2) {
                                            currentState = 2;
                                        }
                                        jsonObject.addProperty("mute", currentState);
                                        MySocketManager.getInstance().getSocket().emit(Const.EVENT_ADD_PARTICIPATED, jsonObject);

                                        PkAudioLiveUserRoot.UsersItem.SeatItem invitedSeat = getSeatAt(invitedPosition);
                                        clearAllSeatsForCurrentUserExcept(invitedSeat);
                                        reserveSeatForCurrentUser(invitedSeat, currentState, MY_UID);
                                        becomeHost(invitedSeat, true);
                                        selfPosition = invitedPosition;
                                        if (currentState == 2) {
                                            isMuteByHost = true;
                                            viewModel.isMuted = true;
                                            binding.btnMute.setEnabled(false);
                                            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.mute_blocked));
                                        } else if (currentState == 1) {
                                            isMuteByHost = false;
                                            viewModel.isMuted = true;
                                            binding.btnMute.setEnabled(true);
                                            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_mute));
                                        } else {
                                            isMuteByHost = false;
                                            viewModel.isMuted = false;
                                            binding.btnMute.setEnabled(true);
                                            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_unmute));
                                        }
                                        if (rtcEngine() != null) {
                                            rtcEngine().muteLocalAudioStream(viewModel.isMuted);
                                        }

                                    } catch (Exception e) {
                                        finishSeatChange();
                                        Log.e(TAG, "onClickContinue: ", e);
                                    }

                                }

                                @Override
                                public void onClickCancel() {

                                }
                            });
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "run: ", e);
                    }
                }

            });

        }

        @Override
        public void onLiveEnd(Object[] args) {
            if (args[0] != null) {
                if (!isFinishing()) {
                    confirmEndLive();
                }
            }
        }

        /** Reactions: host overlay or per-seat overlay (binds view by adapter position safely)*/
        @Override
        public void onReactionReceived(Object[] args1) {
            Log.d(TAG, "onReactionReceived: ");
            runOnUiThread(() -> {
                if (args1[0] != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(args1[0].toString());
                        int rawPosition = jsonObject.optInt("position", -1);
                        if (rawPosition == -1) {
                            UserRoot.User user = new Gson().fromJson(jsonObject.getString("user"), UserRoot.User.class);
                            LiveStramComment liveStramComment = new LiveStramComment("", user, false, host.getLiveStreamingId(), jsonObject.getString("image"), "reaction", "");

                            viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
                            scrollAdapterLogic();

                        } else if (rawPosition == -2) {
                            setUpReaction(jsonObject.getString("image"), binding.imgHostReaction, 7000);
                        } else {
                            RecyclerView.LayoutManager layoutManager = binding.rvSeat.getLayoutManager();
                            if (layoutManager instanceof LinearLayoutManager) {
                                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                                int position = seatIndexFromPayload(jsonObject, currentSeatList());
                                if (position < 0 || position >= seatAdapter.getList().size()) return;
                                // Get the View at the specified position from the LayoutManager
                                View itemView = linearLayoutManager.findViewByPosition(position);
                                // Now you can use itemView to access the ViewBinding object if you have one
                                if (itemView != null) {
                                    @NonNull ItemSeatBinding seatBinding = DataBindingUtil.bind(itemView);
                                    PkAudioLiveUserRoot.UsersItem.SeatItem seatItem = seatAdapter.getList().get(position);
                                    setUpReaction(jsonObject.getString("image"), seatItem, seatBinding.imgHostReaction, 7000);
                                }
                            }

                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        @Override
        public void onRoomNameChange(Object[] args) {
            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(args[0].toString());
                    binding.tvName.setText(jsonObject.getString("roomName"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public void onRoomImageChange(Object[] args) {
            runOnUiThread(() -> {
                host.setRoomImage(args[0].toString());
                Glide.with(WatchAudioLiveActivity.this).load(ImageUrlUtil.normalize(args[0].toString())).into(binding.imgProfile);
            });
        }

    };

    /** Per-seat reaction helper: mark running, load image, auto-clear, notify adapter */
    public void setUpReaction(String image, PkAudioLiveUserRoot.UsersItem.SeatItem seatItem, ImageView imgHostReaction, int duration) {
        // Update the dataset
        seatItem.setReactionImage(image);
        seatItem.setReactionRunning(true);

        Glide.with(this).load(image).into(imgHostReaction);

        // Clear the reaction after the duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            seatItem.setReactionRunning(false);
            seatItem.setReactionImage(null);
            imgHostReaction.setImageDrawable(null);
            seatAdapter.notifyItemChanged(seatAdapter.getList().indexOf(seatItem)); // Update UI for the item
        }, duration);
    }

    /** Host reaction helper: cancel previous if running, schedule auto-clear */
    public void setUpReaction(String image, ImageView imgHostReaction, int duration) {

        // If a reaction is already running, cancel it
        if (isReactionRunning && currentReactionRunnable != null) {
            reactionHandler.removeCallbacks(currentReactionRunnable);
            imgHostReaction.setImageDrawable(null); // Clear the current reaction
            isReactionRunning = false;
        }

        // Set up the new reaction
        isReactionRunning = true;
        Glide.with(this).load(image).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                isReactionRunning = false;
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                // Define a new runnable for the current reaction
                currentReactionRunnable = new Runnable() {
                    @Override
                    public void run() {
                        imgHostReaction.setImageDrawable(null); // Clear the reaction after the duration
                        isReactionRunning = false;
                    }
                };

                // Schedule the runnable to execute after the specified duration
                reactionHandler.postDelayed(currentReactionRunnable, duration);
                return false;
            }
        }).into(imgHostReaction);
    }

    /** Find my current seat from adapter list (null if audience)*/
    private PkAudioLiveUserRoot.UsersItem.SeatItem getSelfPositionFromSeat() {
        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seatList = seatAdapter.getList();
        for (PkAudioLiveUserRoot.UsersItem.SeatItem seatItem : seatList) {
            if (seatItem.isReserved() && seatItem.getUserId() != null && seatItem.getUserId().equals(sessionManager.getUser().getId())) {
                return seatItem;
            }
        }
        return null;
    }

    private PkAudioLiveUserRoot.UsersItem.SeatItem getSeatAt(int position) {
        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seatList = currentSeatList();
        if (position >= 0 && position < seatList.size()) {
            return seatList.get(position);
        }
        for (PkAudioLiveUserRoot.UsersItem.SeatItem seatItem : seatList) {
            if (seatItem.getPosition() == position) {
                return seatItem;
            }
        }
        return null;
    }

    private void applySeatParticipantFromPayload(JSONObject payload) {
        if (payload == null || seatAdapter == null) return;
        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seatList = currentSeatList();
        int position = seatIndexFromPayload(payload, seatList);
        if (position < 0 || position >= seatList.size()) return;

        String userId = payload.optString("userId", "");
        for (int i = 0; i < seatList.size(); i++) {
            if (i != position && userId.equals(seatList.get(i).getUserId())) {
                clearSeatItem(seatList.get(i));
            }
        }

        PkAudioLiveUserRoot.UsersItem.SeatItem seat = seatList.get(position);
        seat.setReserved(true);
        seat.setName(payload.optString("name", ""));
        seat.setImage(payload.optString("image", ""));
        seat.setAvatarFrame(payload.optString("avatarFrame", payload.optString("avatarFrameImage", "")));
        seat.setCountry(payload.optString("country", ""));
        seat.setAgoraUid(payload.optInt("agoraUid", 0));
        seat.setMute(payload.optInt("mute", 0));
        seat.setUserId(userId);
        seat.setSpeaking(false);
        bookedSeatItemList = seatList;
        seatAdapter.updateData(seatList);
        finishSeatChange();
    }

    private void clearSeatAt(int position) {
        if (seatAdapter == null) return;
        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seatList = currentSeatList();
        if (position < 0 || position >= seatList.size()) return;
        clearSeatItem(seatList.get(position));
        bookedSeatItemList = seatList;
        seatAdapter.updateData(seatList);
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
        if (seat == null || sessionManager == null || sessionManager.getUser() == null) {
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
        if (bookedSeatItemList != null && !bookedSeatItemList.isEmpty()) {
            seatAdapter.updateData(bookedSeatItemList);
        } else {
            seatAdapter.updateData(new ArrayList<>(seatAdapter.getList()));
        }
    }

    SocketConnectHandler socketConnectHandler = new SocketConnectHandler() {
        @Override
        public void onConnect() {
            if (host != null) {
                addLessView(true);
            }
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
            emitLiveRejoin();
            if (host != null) {
                addLessView(true);
            }
        }
    };

    private void emitLiveRejoin() {
        if (host == null || sessionManager == null || sessionManager.getUser() == null || MySocketManager.getInstance().getSocket() == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("liveStreamingId", host.getLiveStreamingId());
            jsonObject.put("liveHistoryId", host.getLiveStreamingId());
            jsonObject.put("liveUserMongoId", host.getId());
            jsonObject.put("userId", sessionManager.getUser().getId());
            MySocketManager.getInstance().getSocket().emit(Const.LIVE_REJOIN, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "emitLiveRejoin: ", e);
        }
    }

    private void scrollAdapterLogic() {
        binding.rvComments.scrollToPosition(0);
    }

    private void putLiveRoomKeys(JSONObject jsonObject) throws JSONException {
        jsonObject.put("liveStreamingId", host.getLiveStreamingId());
        jsonObject.put("liveHistoryId", host.getLiveStreamingId());
        jsonObject.put("liveUserMongoId", host.getId());
    }

    private void putLiveRoomKeys(JsonObject jsonObject) {
        jsonObject.addProperty("liveStreamingId", host.getLiveStreamingId());
        jsonObject.addProperty("liveHistoryId", host.getLiveStreamingId());
        jsonObject.addProperty("liveUserMongoId", host.getId());
    }

    private List<PkAudioLiveUserRoot.UsersItem.SeatItem> currentSeatList() {
        if (bookedSeatItemList != null && !bookedSeatItemList.isEmpty()) {
            return bookedSeatItemList;
        }
        if (host != null && host.getSeat() != null && !host.getSeat().isEmpty()) {
            return host.getSeat();
        }
        return seatAdapter != null ? seatAdapter.getList() : Collections.emptyList();
    }

    private void putSeatIndex(JsonObject jsonObject, int adapterIndex) {
        jsonObject.addProperty("position", adapterIndex);
        jsonObject.addProperty("seatIndex", adapterIndex);
        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seats = currentSeatList();
        if (adapterIndex >= 0 && adapterIndex < seats.size()) {
            jsonObject.addProperty("seatPosition", seats.get(adapterIndex).getPosition());
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

    private int seatIndexForSeat(PkAudioLiveUserRoot.UsersItem.SeatItem targetSeat) {
        if (targetSeat == null) return -1;
        List<PkAudioLiveUserRoot.UsersItem.SeatItem> seats = currentSeatList();
        int index = seats.indexOf(targetSeat);
        if (index >= 0) return index;
        String userId = targetSeat.getUserId();
        for (int i = 0; i < seats.size(); i++) {
            PkAudioLiveUserRoot.UsersItem.SeatItem seat = seats.get(i);
            if (!TextUtils.isEmpty(userId) && userId.equals(seat.getUserId())) return i;
            if (seat.getPosition() == targetSeat.getPosition()) return i;
        }
        return -1;
    }

    private void clearAllSeatsForCurrentUserExcept(PkAudioLiveUserRoot.UsersItem.SeatItem keepSeat) {
        if (sessionManager == null || sessionManager.getUser() == null) return;
        String localUserId = sessionManager.getUser().getId();
        if (TextUtils.isEmpty(localUserId)) return;
        for (PkAudioLiveUserRoot.UsersItem.SeatItem seat : currentSeatList()) {
            if (seat != keepSeat && localUserId.equals(seat.getUserId())) {
                clearSeatItem(seat);
            }
        }
    }

    private int agoraUidForCurrentUser() {
        String userId = sessionManager != null && sessionManager.getUser() != null ? sessionManager.getUser().getId() : "";
        long hash = TextUtils.isEmpty(userId) ? System.currentTimeMillis() : (userId.hashCode() & 0xffffffffL);
        return (int) (1000 + (hash % 2000000000L));
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_watch_audio_live);

        MySocketManager.getInstance().addAudioRoomHandler(audioRoomHandler);
        MySocketManager.getInstance().addSocketConnectHandler(socketConnectHandler);
        giftReceiveAdapter = new GiftReceiveAdapter(WatchAudioLiveActivity.this);

        giftViewModel = ViewModelProviders.of(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);
        ReactionsViewModel reactionsViewModel = ViewModelProviders.of(this, new ViewModelFactory(new ReactionsViewModel()).createFor()).get(ReactionsViewModel.class);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new WatchLiveViewModel()).createFor()).get(WatchLiveViewModel.class);

        sessionManager = new SessionManager(this);
        binding.setViewModel(viewModel);
        viewModel.initLister();
        giftViewModel.initEmojiSheet(this);
        giftViewModel.getGiftCategory();

        bottomSheetReactions = new BottomSheetReactions(this);
        reactionsViewModel.loadReactions(bottomSheetReactions::loadData);

        Intent intent = getIntent();
        String userStr = intent.getStringExtra(Const.DATA);
        boolean isNotification = intent.getBooleanExtra(Const.isNotification, false);

        if (isNotification) {
            ((MainApplication) getApplication()).initAgora(WatchAudioLiveActivity.this);
        }

        if (userStr != null && !userStr.isEmpty()) {
            host = new Gson().fromJson(userStr, PkAudioLiveUserRoot.UsersItem.class);
            if (host.getToken() != null){
                token = host.getToken();
            }

            binding.tvRcoins.setText(RayziUtils.formatCoin(host.getRCoin()));
            binding.tvName.setText(host.getRoomName());
            RayziUtils.marqueeText(binding.tvName);
            binding.tvUniqueId.setText(getString(R.string.id) + host.getUniqueId());
            Glide.with(this).load(ImageUrlUtil.normalize(host.getRoomImage())).apply(MainApplication.requestOptions).circleCrop().into(binding.imgProfile);

            gridLayoutManager = new GridLayoutManager(this, 4);
            binding.rvSeat.setLayoutManager(gridLayoutManager);
            seatAdapter = new SeatAdapter(WatchAudioLiveActivity.this, sessionManager,host.getLiveUserId());
            binding.rvSeat.setAdapter(seatAdapter);
            binding.mainHostnameCount.setText(host.getName());

            if (host.getBackground() != null && !host.getBackground().isEmpty()) {
                Glide.with(WatchAudioLiveActivity.this).load(ImageUrlUtil.normalize(host.getBackground())).thumbnail(Glide.with(WatchAudioLiveActivity.this).load(ImageUrlUtil.normalize(host.getBackground()))).placeholder(R.drawable.default_bg_audioroom).into(binding.mainImg);
            } else {
                Glide.with(WatchAudioLiveActivity.this).load(R.drawable.default_bg_audioroom).into(binding.mainImg);
            }

            if (isMyServiceRunning()) {
                stopService(new Intent(WatchAudioLiveActivity.this, FloatingButtonService.class));
            }
            seatAdapter.addData(host.getSeat());

            seatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    binding.rvSeat.post(() -> {
                        if (seatAdapter.getItemCount() > 0) {
                            binding.rvSeat.scrollToPosition(seatAdapter.getItemCount() - 1);
                        }
                    });
                }
            });

            PkAudioLiveUserRoot.UsersItem.SeatItem selfPos = getSelfPositionFromSeat();

            if (selfPos != null) {
                MY_UID = selfPos.getAgoraUid();
            } else {
                MY_UID = agoraUidForCurrentUser();
            }


            initView();
            initLister();
            if (host.getPrivateCode() != 0) {

                float radius = 1f;
                View decorView = getWindow().getDecorView();
                ViewGroup rootView = decorView.findViewById(android.R.id.content);
                Drawable windowBackground = decorView.getBackground();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    binding.blurView.setupWith(rootView, new RenderEffectBlur()) // or RenderEffectBlur
                            .setFrameClearDrawable(windowBackground) // Optional
                            .setBlurRadius(radius);
                }

                new BottomSheetAudioRoomPasscode(this, host, privateCode -> {
                    if (privateCode != 0) {
                        addLessView(true);
                        if (selfPos == null) {
                            joinChannel();
                        }
                        binding.blurView.setVisibility(GONE);
                    } else {
                        if (rtcEngine() != null) {
                            rtcEngine().leaveChannel();
                        }
                        finish();
                    }
                });
            } else {
                addLessView(true);
                if (selfPos == null) {
                    joinChannel();
                }
            }

            BaseActivity.STATUS_LIVE = true;
            binding.rvGift.setAdapter(giftReceiveAdapter);

            if (host.getRoomWelcome() != null) {
                viewModel.liveStramCommentAdapter.addSingleComment(null);
                LiveStramComment liveStreamComment1 = new LiveStramComment(getString(R.string.announcement) + host.getRoomWelcome(), sessionManager.getUser(), true, host.getLiveStreamingId(), "", "comment", "");
                viewModel.liveStramCommentAdapter.addSingleComment(liveStreamComment1);
            } else {
                viewModel.liveStramCommentAdapter.addSingleComment(null);
                LiveStramComment liveStreamComment1 = new LiveStramComment(getString(R.string.announcement_welcome_to_room), sessionManager.getUser(), true, host.getLiveStreamingId(), "", "comment", "");
                viewModel.liveStramCommentAdapter.addSingleComment(liveStreamComment1);
            }


            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(false);   // anchor at bottom
            layoutManager.setReverseLayout(true); // natural order
            binding.rvComments.setLayoutManager(layoutManager);
            binding.rvComments.setAdapter(viewModel.liveStramCommentAdapter);
            binding.btnSend.setOnClickListener(this::onClickSendComment);
            binding.commentInputContainer.setOnClickListener(v -> showCommentKeyboard());
            binding.etComment.setOnClickListener(v -> showCommentKeyboard());

            binding.rvComments.scrollToPosition(viewModel.liveStramCommentAdapter.getItemCount() - 1);

            if (host.getAudioRoomConfig() != null && host.getAudioRoomConfig().isHostMute() == 1) {
                binding.ivMute.setVisibility(VISIBLE);
            } else {
                rtcEngine().setEnableSpeakerphone(true);

                AudioManager audioManager;
                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    audioManager.setSpeakerphoneOn(true);
                }

                binding.ivMute.setVisibility(GONE);
            }

            handleIfIamHost(selfPos);
        }

        setupOptionsToggle();
        autoHideWhenKeyboardShown(findViewById(R.id.lytButtons));
    }

    public void onClickBack(View view) {
        onBackPressed();
    }

    public void onClickSendComment(View view) {

        String comment = binding.etComment.getText().toString();
        if (!comment.isEmpty()) {
            binding.etComment.setText("");
            LiveStramComment liveStramComment = new LiveStramComment(comment, sessionManager.getUser(), false, host.getLiveStreamingId(), "", "comment", "");
            rememberLocalComment(comment);
            viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
            scrollAdapterLogic();
            try {
                JSONObject commentPayload = new JSONObject(new Gson().toJson(liveStramComment));
                putLiveRoomKeys(commentPayload);
                commentPayload.put("userId", sessionManager.getUser().getId());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_COMMENT_AUDIO, commentPayload);
            } catch (JSONException e) {
                Log.e(TAG, "onClickSendComment: ", e);
            }
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

    /** If already on a seat when opening: emit participate, assume role, set correct mute UI/state*/
    private void handleIfIamHost(PkAudioLiveUserRoot.UsersItem.SeatItem selfPos) {
        if (selfPos != null) {
            MY_UID = selfPos.getAgoraUid();
            JsonObject jsonObject = new JsonObject();
            int initialSeatIndex = seatIndexForSeat(selfPos);
            if (initialSeatIndex < 0) return;
            putSeatIndex(jsonObject, initialSeatIndex);
            putLiveRoomKeys(jsonObject);
            jsonObject.addProperty("userId", sessionManager.getUser().getId());
            jsonObject.addProperty("name", sessionManager.getUser().getName());
            jsonObject.addProperty("country", sessionManager.getUser().getCountry());
            jsonObject.addProperty("agoraUid", MY_UID);
            jsonObject.addProperty("mute", selfPos.isMute());
            jsonObject.addProperty("image", sessionManager.getUser().getImage());
            jsonObject.addProperty("avatarFrame", sessionManager.getUser().getAvatarFrameImage());

            MySocketManager.getInstance().getSocket().emit(Const.EVENT_ADD_PARTICIPATED, jsonObject);
            Log.d(TAG, "doWork: add participate emit " + jsonObject);

            becomeHost(selfPos, false);
            selfPosition = initialSeatIndex;

            if (selfPos.isMute() == 2) {
                isMuteByHost = true;
                viewModel.isMuted = true;
                binding.btnMute.setEnabled(false);
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.mute_blocked));
            } else if (selfPos.isMute() == 1) {
                isMuteByHost = false;
                viewModel.isMuted = true;
                binding.btnMute.setEnabled(true);
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_mute));
            } else {
                isMuteByHost = false;
                viewModel.isMuted = false;
                binding.btnMute.setEnabled(true);
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_unmute));
            }
            if (rtcEngine() != null) {
                rtcEngine().muteLocalAudioStream(viewModel.isMuted);
            }
        }
    }

    /** Switch to broadcaster role with audio-only, keep speakerphone path & normalized playback*/
    private void becomeHost(PkAudioLiveUserRoot.UsersItem.SeatItem seatItem, boolean fromOnSeatClicked) {
        if (rtcEngine() != null) {
            isHost = true;
            rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            rtcEngine().enableAudio();
            rtcEngine().disableVideo();
            rtcEngine().muteLocalAudioStream(viewModel.isMuted);
            rtcEngine().adjustRecordingSignalVolume(viewModel.isMuted ? 0 : 100);

            // keep exactly the same output path & playback volume as audience
            ensureSpeakerphone();
            rtcEngine().adjustPlaybackSignalVolume(250);
            normalizePlaybackVolume();
        }
    }

    private void addLessView(boolean isAdd) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("liveStreamingId", host.getLiveStreamingId());
            jsonObject.put("liveHistoryId", host.getLiveStreamingId());
            jsonObject.put("liveUserMongoId", host.getId());
            jsonObject.put("userId", sessionManager.getUser().getId());
            jsonObject.put("isVIP", sessionManager.getUser().isIsVIP());
            jsonObject.put("image", sessionManager.getUser().getImage());
            jsonObject.put("name", sessionManager.getUser().getName());
            jsonObject.put("gender", sessionManager.getUser().getGender());
            jsonObject.put("country", sessionManager.getUser().getCountry());
            jsonObject.put("userName", sessionManager.getUser().getName());
            jsonObject.put("avatarFrame", sessionManager.getUser().getAvatarFrameImage());
            jsonObject.put("entrySvga", sessionManager.getUser().getSvgaImage());
            jsonObject.put("isUserBackgroundLive", sessionManager.getIsUserBackgroundLive());
            jsonObject.put("fcmToken", sessionManager.getUser().getFcmToken());
            jsonObject.put("notification", sessionManager.isNotificationOn());
            if (isAdd) {
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_ADD_VIEW, jsonObject);
            } else {
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_VIEW, jsonObject);
            }

        } catch (JSONException e) {
            Log.e(TAG, "addLessView: ", e);
        }
    }

    private void joinChannel() {
        try {
            rtcEngine().setDefaultAudioRoutetoSpeakerphone(true);

            Log.d(TAG, "joinChannel:  ");
            if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
                token = null; // default, no token
            }
            String tkn = RtcTokenBuilderSample.main(host.getChannel() + "audio", sessionManager.getSetting().getAgoraKey(), sessionManager.getSetting().getAgoraCertificate());
            if (rtcEngine() != null) {
                rtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

                rtcEngine().setAudioProfile(
                        Constants.AUDIO_PROFILE_SPEECH_STANDARD,      // mono, speech tuned
                        Constants.AUDIO_SCENARIO_CHATROOM        // ensures voice route
                );

                rtcEngine().disableVideo();
                rtcEngine().enableAudioVolumeIndication(1000, 3, false);
                Log.d(TAG, "joinChannel: ");

                ensureSpeakerphone();
                normalizePlaybackVolume();

                rtcEngine().joinChannel(tkn, host.getChannel() + "audio", "", MY_UID);
                forceAudienceListenOnly();

                rtcEngine().adjustPlaybackSignalVolume(250);

                rtcEngine().setEnableSpeakerphone(true);

                AudioManager audioManager;
                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    audioManager.setSpeakerphoneOn(true);
                }

                LiveStramComment liveStramComment = new LiveStramComment("", sessionManager.getUser(), true, host.getLiveStreamingId(), "", "comment", "");
                if (!sessionManager.getBooleanValue("isUserKeep")) {
                    try {
                        JSONObject commentPayload = new JSONObject(new Gson().toJson(liveStramComment));
                        putLiveRoomKeys(commentPayload);
                        commentPayload.put("userId", sessionManager.getUser().getId());
                        MySocketManager.getInstance().getSocket().emit(Const.EVENT_COMMENT_AUDIO, commentPayload);
                    } catch (JSONException e) {
                        Log.e(TAG, "joinChannel: emit join comment", e);
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "joinChannel: ", e);
        }
    }

    private void initView() {
        emojiBottomsheetFragment = new EmojiBottomSheetFragment(true);
        userProfileBottomSheet = new UserProfileBottomSheet(this);

        if (rtcEngine() == null) return;

        forceAudienceListenOnly();
        ensureSpeakerphone();
        rtcEngine().adjustPlaybackSignalVolume(250);
        normalizePlaybackVolume();
        isHost = false;
    }

    private void forceAudienceListenOnly() {
        if (rtcEngine() == null) return;
        isHost = false;
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        rtcEngine().enableAudio();
        rtcEngine().disableVideo();
        rtcEngine().muteLocalAudioStream(true);
        rtcEngine().adjustRecordingSignalVolume(0);
    }

    private void initLister() {

        binding.imgGame.setOnClickListener(v -> {
            new BottomSheetGameList(this, (gameItem, position) -> {
                if (position == 0) {
                    new BottomSheetGameCasino(this, gameItem.getLink(), new BottomSheetGameCasino.OnDialogDismissListener() {
                        @Override
                        public void onDismiss() {
                            MySocketManager.getInstance().getSocket().emit(Const.USER_COIN_UPDATE, sessionManager.getUser().getId());
                            Log.d(TAG, "onDismiss: couns:..." + sessionManager.getUser().getDiamond());
                        }
                    });
                } else if (position == 1) {
                    new DialogGame(this, gameItem.getLink(), new DialogGame.OnDialogDismissListener() {
                        @Override
                        public void onDismiss() {
                            MySocketManager.getInstance().getSocket().emit(Const.USER_COIN_UPDATE, sessionManager.getUser().getId());
                        }
                    });
                } else {
                    new BottomSheetGameTeenPatti(this, gameItem.getLink(), new BottomSheetGameTeenPatti.OnDialogDismissListener() {
                        @Override
                        public void onDismiss() {
                            MySocketManager.getInstance().getSocket().emit(Const.USER_COIN_UPDATE, sessionManager.getUser().getId());
                        }
                    });
                }
            });
        });
        binding.btnMute.setOnClickListener(v -> {

            PkAudioLiveUserRoot.UsersItem.SeatItem selfItem = getSelfPositionFromSeat();
            if (selfItem == null || selfPosition == -1) {
                forceAudienceListenOnly();
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_mute));
                Toast.makeText(this, "Choose a seat first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selfItem != null && selfItem.isMute() == 2) {
                Toast.makeText(this, getString(R.string.you_cant_unmute_your_self), Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.isMuted = !viewModel.isMuted;
            if (rtcEngine() != null) {
                int kk = rtcEngine().muteLocalAudioStream(viewModel.isMuted);
                rtcEngine().adjustRecordingSignalVolume(viewModel.isMuted ? 0 : 100);
                Log.e(TAG, "initLister:   " + kk);
            }
            JsonObject jsonObject = getJsonObject((viewModel.isMuted) ? 1 : 0);
            if (selfPosition != -1) {
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_MUTE_SEAT, jsonObject);
            }
            if (viewModel.isMuted) {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_mute));
            } else {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_unmute));
            }
        });

        viewModel.clickedComment.observe(this, user -> {
            getUser(user.getId());
        });
        viewModel.clickedUser.observe(this, user -> {
            try {
                getUser(user.get("userId").toString());
            } catch (JSONException e) {
                Log.e(TAG, "joinChannel: ", e);
            }
        });


        binding.lytHost.setOnClickListener(view -> {
//            getUser(host.getLiveUserId());

            BottomsheetGuestUserProfile bs = new BottomsheetGuestUserProfile(host.getLiveUserId(),true);
            bs.show(this.getSupportFragmentManager(), "guestProfile");
        });

        binding.ivShare.setOnClickListener(v -> {
            binding.ivShare.setEnabled(false);

            String deepLink = BuildConfig.BASE_URL + "open?type=AUDIO_LIVE&userId=" + host.getLiveUserId() + "&joinUserId=" + sessionManager.getUser().getId() + "&liveStreamingId=" + host.getLiveStreamingId() + "&livetype=audio";

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
                jsonObject.put("position", selfPosition);
                jsonObject.put("seatIndex", selfPosition);
                List<PkAudioLiveUserRoot.UsersItem.SeatItem> seats = currentSeatList();
                if (selfPosition >= 0 && selfPosition < seats.size()) {
                    jsonObject.put("seatPosition", seats.get(selfPosition).getPosition());
                }
                jsonObject.put("image", reaction.getImage());
                jsonObject.put("user", new Gson().toJson(sessionManager.getUser()));
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_SEND_REACTION, jsonObject);
            } catch (Exception e) {
                Log.e(TAG, "initLister: ", e);
            }
        });

        giftViewModel.finalGift.observe(this, giftItem -> {
            if (giftItem != null) {
                double totalCoin = giftItem.getCoin() * giftItem.getCount();
                if (sessionManager.getUser().getDiamond() < totalCoin) {
                    Toast.makeText(WatchAudioLiveActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!giftViewModel.userListAdapter.getCurrentList().isEmpty()) {
                    List<String> selectedUsers = giftViewModel.userListAdapter.getCurrentList().stream()
                            .filter(UserSelectableClass::isSelected)
                            .map(user -> user.getSeatItem().getUserId())
                            .collect(Collectors.toList());

                    List<String> selectedUsersName = giftViewModel.userListAdapter.getCurrentList().stream()
                            .filter(UserSelectableClass::isSelected)
                            .map(user -> user.getSeatItem().getName())
                            .collect(Collectors.toList());

                    if (selectedUsers.isEmpty()) {
                        Toast.makeText(this, getString(R.string.select_at_least_one_user), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("senderUserId", sessionManager.getUser().getId());
                        jsonObject.put("receiverUserId", Arrays.toString(selectedUsers.toArray()));
                        jsonObject.put("hostId", host.getLiveUserId());
                        putLiveRoomKeys(jsonObject);
                        jsonObject.put("userName", sessionManager.getUser().getName());
//                        jsonObject.put("receiverUserName", Arrays.toString(selectedUsersName.toArray()));
                        jsonObject.put("receiverUserName", String.join(",", selectedUsersName));
                        jsonObject.put("coin", giftItem.getCoin() * giftItem.getCount());
                        jsonObject.put("gift", new Gson().toJson(giftItem));
                        jsonObject.put("giftCount", giftItem.getCount());
                        jsonObject.put("timeStamp", System.currentTimeMillis());
                        jsonObject.put("liveType", "audio");
                        int i = selectedUsers.size();
                        double totalGiftCoin = giftItem.getCoin() * giftItem.getCount() * i;
                        double totalDiamond = sessionManager.getUser().getDiamond();

                        if (totalDiamond >= totalGiftCoin) {
                            MySocketManager.getInstance().getSocket().emit(Const.EVENT_NORMAL_USER_GIFT, jsonObject);
                            Log.d(TAG, "GIFT Emitted: ==> " + jsonObject.toString());
                        } else {
                            Toast.makeText(WatchAudioLiveActivity.this, getString(R.string.you_not_have_enough_diamonds_to_send_gift), Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "initLister: ==> gift emitted");
                        emojiBottomsheetFragment.dismiss();
                    } catch (JSONException e) {
                        Log.e(TAG, "joinChannel: ", e);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.don_t_have_user_to_sent_a_gift_wait_for_user), Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnGift.setOnClickListener(v -> {
            if (!emojiBottomsheetFragment.isAdded()) {
                giftViewModel.users.clear();
                giftViewModel.users.add(new UserSelectableClass(new PkAudioLiveUserRoot.UsersItem.SeatItem(host.getImage(), host.getCountry(), true,
                        "Host", false, host.getAgoraUID(), 0, true, host.getId(), -1, false, host.getLiveUserId())));
                host.getSeat().stream().filter(PkAudioLiveUserRoot.UsersItem.SeatItem::isReserved).map(UserSelectableClass::new).forEach(giftViewModel.users::add);
                emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojifragfmetn");
            }
        });

        seatAdapter.setOnSeatClick((seatItem, position) -> {
            Log.d(TAG, "OnClickSeat: isMute ==>  " + seatItem.isMute());
            String seatUserId = seatItem.getUserId();
            String localUserId = sessionManager.getUser().getId();
            if (!isMuteByHost && seatUserId != null && seatUserId.equals(localUserId)) {
                if (rtcEngine() != null) {
                    rtcEngine().muteLocalAudioStream(seatItem.isMute() != 0);
                    rtcEngine().adjustRecordingSignalVolume(seatItem.isMute() != 0 ? 0 : 100);
                }
            }
            doWork(seatItem, position);

        });
    }

    /**   Seat tap handler: self-remove OR show locked/reserved popups OR take seat directly,
     then set local mute UI/state per host/self/none*/
    private void doWork(PkAudioLiveUserRoot.UsersItem.SeatItem seatItem, int i) {
        Log.d(TAG, "doWork: isReserved " + seatItem.isReserved());

        if (seatItem.isReserved() && seatItem.getUserId() != null && seatItem.getUserId().equalsIgnoreCase(sessionManager.getUser().getId())) {
            new PopupBuilder(WatchAudioLiveActivity.this).showRemovePopup(() -> {
                if (!beginSeatChange()) return;
                JsonObject jsonObject = new JsonObject();
                putSeatIndex(jsonObject, i);
                putLiveRoomKeys(jsonObject);
                jsonObject.addProperty("userId", sessionManager.getUser().getId());
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);
                clearSeatAt(i);
                Log.d(TAG, "doWork: become audience");
                if (rtcEngine() != null) {
                    rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                    rtcEngine().disableVideo();
                }
                isHost = false;
                selfPosition = -1;
            });
            return;
        }

        if (seatItem.isReserved()) {
            if (seatItem.getUserId() != null && !seatItem.getUserId().isEmpty()) {
                getUser(seatItem.getUserId());
            } else {
                new PopupBuilder(WatchAudioLiveActivity.this).showPopUpWithVector(R.drawable.ic_no_seat, getString(R.string.this_seat_is_reserved), getString(R.string.please_choose_another_seat), getString(R.string.okay), () -> {
                });
            }
        } else if (seatItem.isLock()) {
            new PopupBuilder(WatchAudioLiveActivity.this).showPopUpWithVector(R.drawable.audio_lock, getString(R.string.this_seat_is_locked_by_host), getString(R.string.please_choose_another_seat), getString(R.string.okay), () -> {
            });
        } else {
            if (!beginSeatChange()) return;
            JsonObject jsonObject = new JsonObject();
            putSeatIndex(jsonObject, i);
            putLiveRoomKeys(jsonObject);
            jsonObject.addProperty("liveUserId", host.getLiveUserId());
            jsonObject.addProperty("userId", sessionManager.getUser().getId());
            jsonObject.addProperty("name", sessionManager.getUser().getName());
            jsonObject.addProperty("country", sessionManager.getUser().getCountry());
            jsonObject.addProperty("agoraUid", MY_UID);

            int currentState = seatItem.isMute();
            if (currentState == 0) {
                currentState = viewModel.isMuted ? 1 : 0;
            }
            PkAudioLiveUserRoot.UsersItem.SeatItem selfPos = getSelfPositionFromSeat();
            if (selfPos != null && selfPos != seatItem) {
                int oldPosition = seatAdapter.getList().indexOf(selfPos);
                if (oldPosition >= 0) {
                    JsonObject removeOldSeat = new JsonObject();
                    putSeatIndex(removeOldSeat, oldPosition);
                    putLiveRoomKeys(removeOldSeat);
                    removeOldSeat.addProperty("userId", sessionManager.getUser().getId());
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, removeOldSeat);
                }
            }
            if (selfPos != null && selfPos.isMute() == 2) {
                currentState = 2;
            }

            jsonObject.addProperty("mute", currentState);
            jsonObject.addProperty("image", sessionManager.getUser().getImage());
            jsonObject.addProperty("avatarFrame", sessionManager.getUser().getAvatarFrameImage());

            clearAllSeatsForCurrentUserExcept(seatItem);
            reserveSeatForCurrentUser(seatItem, currentState, MY_UID);
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_ADD_PARTICIPATED, jsonObject);
            becomeHost(seatItem, true);
            selfPosition = i;

            if (currentState == 2) {
                isMuteByHost = true;
                viewModel.isMuted = true;
                binding.btnMute.setEnabled(false);
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.mute_blocked));
            } else if (currentState == 1) {
                isMuteByHost = false;
                viewModel.isMuted = true;
                binding.btnMute.setEnabled(true);
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_mute));
            } else {
                isMuteByHost = false;
                viewModel.isMuted = false;
                binding.btnMute.setEnabled(true);
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.ic_unmute));
            }
            if (rtcEngine() != null) {
                rtcEngine().muteLocalAudioStream(viewModel.isMuted);
                rtcEngine().adjustRecordingSignalVolume(viewModel.isMuted ? 0 : 100);
            }
            Log.d(TAG, "doWork: add participate emit " + jsonObject);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }

    private void getUser(String userId) {
        customDialogClass.show();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fromUserId", sessionManager.getUser().getId());
            jsonObject.put("toUserId", userId);
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_GET_USER, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "joinChannel: ", e);
        }
    }

    @Override
    public void onBackPressed() {
        endLive();
    }

    /** Leave popup: either exit cleanly or minimize into floating bubble preserving mute*/
    private void endLive() {
        Log.d(TAG, "endLive: ma jay che ==> ");
        new PopupBuilder(this).showLiveEndPopup(new PopupBuilder.OnMultiButtonPopupLister() {
            @Override
            public void onClickContinue() {
                confirmEndLive();
                sessionManager.setIsUserBackgroundLive(false);
                finish();
            }

            @Override
            public void onClickCancel() {
                if (checkOverlayDisplayPermission()) {
                    if (rtcEngine() != null) {
                        rtcEngine().muteLocalAudioStream(viewModel.isMuted);
                    }
                    sessionManager.setIsUserBackgroundLive(true);
                    sessionManager.saveUserAudioBgModel(host);
                    sessionManager.saveBooleanValue("isUserKeep", true);
                    startService(new Intent(WatchAudioLiveActivity.this, FloatingButtonService.class).putExtra("image", host.getImage()));
                    finish();
                } else {
                    requestOverlayDisplayPermission();
                }

            }
        });
    }

    /** Final exit: emit less-participated, leave channel, remove view from counts*/
    private void confirmEndLive() {

        JsonObject jsonObject = new JsonObject();
        putSeatIndex(jsonObject, selfPosition);
        putLiveRoomKeys(jsonObject);
        jsonObject.addProperty("userId", sessionManager.getUser().getId());

        MySocketManager.getInstance().getSocket().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);

        if (rtcEngine() != null) {
            rtcEngine().leaveChannel();
        }

        addLessView(false);
        sessionManager.saveBooleanValue("isUserKeep", false);
        BaseActivity.STATUS_LIVE = false;
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MySocketManager.getInstance().removeAudioRoomHandler(audioRoomHandler);
        MySocketManager.getInstance().removeSocketConnectHandler(socketConnectHandler);
        seatChangeHandler.removeCallbacks(clearSeatChangePending);
        BaseActivity.STATUS_LIVE = false;
    }

    public void onClickReport(View view) {
        new BottomSheetReport(this, host.getLiveUserId(), () -> {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_layout, findViewById(R.id.layout_custom_toast));
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        Log.d(TAG, "onFirstRemoteVideoDecoded: elapsed ==> " + elapsed);
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        Log.d(TAG, "onLeaveChannel: stats" + stats);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.d(TAG, "onJoinChannelSuccess: isUserJoined  " + isUserJoined);
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
        Log.d(TAG, "onUserJoined: ma jay che ===");
        Log.d(TAG, "onUserJoined: " + uid + "  elapsed" + elapsed);
        binding.rvSeat.setVisibility(VISIBLE);
        binding.mining.setVisibility(GONE);
        isUserJoined = true;
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
        if (!statsManager().isEnabled()) return;


    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        if (!statsManager().isEnabled()) return;

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
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
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

    }

    @Override
    public void onFirstRemoteAudioFrame(int uid, int elapsed) {

    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {

    }

    /** Map Agora volume callbacks: host ring when uid==1, route others to SeatAdapter VAD*/
    @Override
    public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {
        Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: <==> NEW SESSION <==>");

        Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: speakers.length  " + speakers.length);
        Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: totalVolume  " + totalVolume);


        runOnUiThread(() -> {
            if (totalVolume <= 0) return;

            for (IRtcEngineEventHandler.AudioVolumeInfo info : speakers) {
                Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: uid " + info.uid);
                Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: volume" + info.volume);
                    if (isHost && info.uid == 0) {
                        Log.d(TAG, "onAudioVolumeIndication: I AM HOST");
                        info.uid = MY_UID;

                    } else {
                        Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: OTHER SPEAKING");
                    }
                    seatAdapter.onAudioVolumeIndicationSingle(info);

            }

        });
    }

    @Override
    public void onActiveSpeaker(int uid) {

    }

    @Override
    public void onAudioMixingStateChanged(int state, int reason) {

    }

    @Override
    public void onTokenPrivilegeWillExpire(String token) {
        Log.d(TAG, "onTokenPrivilegeWillExpire: ");
        try {
            String tkn = RtcTokenBuilderSample.main(host.getChannel() + "audio", sessionManager.getSetting().getAgoraKey(), sessionManager.getSetting().getAgoraCertificate());
            rtcEngine().renewToken(tkn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onRequestToken() {

    }

    /** Keep output on speakerphone when no BT/wired device is active*/
    @Override
    public void onAudioRouteChanged(int routing) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;

        // If there’s no external device, make sure we stay on speakerphone
        if (!hasExternalAudioRoute(am)) {
            ensureSpeakerphone();
        }
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();
    }

    /** Check if an external output route exists (BT/wired); legacy fallbacks pre-M*/
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

    /** Force comms mode + set built-in speaker as comm device on Android 12L+, fallback pre-S*/
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

    @Override
    protected void onResume() {
        super.onResume();
        configureAudioRouting(rtcEngine());

        PkAudioLiveUserRoot.UsersItem.SeatItem selfSeat = getSelfPositionFromSeat();
        if (selfSeat == null) {
            forceAudienceListenOnly();
            return;
        }

        if (selfSeat != null) {
            Log.d(TAG, "onResume:  isMute " + selfSeat.isMute());
            if (selfSeat.isMute() == 1 || selfSeat.isMute() == 2) {
                Log.d(TAG, "onResume:  1 or 2 ? " + selfSeat.isMute());
                return;
            }
        }
        boolean forcedByHost = sessionManager.getBooleanValue("isMuteByHost");
        Log.d(TAG, "onResume: forcedByHost  " + forcedByHost);

        if (forcedByHost) {
            // match what FloatingButtonService did
            isMuteByHost = true;
            if (rtcEngine() != null) rtcEngine().muteLocalAudioStream(true);

            viewModel.isMuted = true;
            // disable local toggle & show blocked icon
            binding.btnMute.setEnabled(false);
            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mute_blocked));
            binding.ivMute.setVisibility(View.VISIBLE);

            JsonObject jsonObject = getJsonObject(2);
            if (selfPosition != -1) {
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_MUTE_SEAT, jsonObject);
            }
        }
    }

    /** Build common mute payload for EVENT_MUTE_SEAT (my seat/ids/agora/mute value)*/
    @NonNull
    private JsonObject getJsonObject(int value) {
        JsonObject jsonObject = new JsonObject();
        putSeatIndex(jsonObject, selfPosition);
        putLiveRoomKeys(jsonObject);
        jsonObject.addProperty("liveUserId", host.getLiveUserId());
        jsonObject.addProperty("agoraId", MY_UID);
        jsonObject.addProperty("mute", value);
        jsonObject.addProperty("mutedUserId", sessionManager.getUser().getId());
        return jsonObject;
    }
}
