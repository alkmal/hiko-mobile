package com.codder.ultimate.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import com.codder.ultimate.SvgaEntryManager;
import com.codder.ultimate.adapter.ScreenSlidePagerAdapter;
import com.codder.ultimate.ads.MyRewardAds;
import com.codder.ultimate.databinding.ActivityMainBinding;
import com.codder.ultimate.fake.activity.FakeAudioWatchActivity;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.live.activity.GotoLiveActivity;
import com.codder.ultimate.live.activity.WatchAudioLiveActivity;
import com.codder.ultimate.live.model.GiftCategoryRoot;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.model.LiveStreamRoot;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.live.model.ThemeRoot;
import com.codder.ultimate.live.utils.LiveHandler;
import com.codder.ultimate.modelclass.BroadcastBannerRoot;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.post.activity.FeedListActivity;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.profile.modelclass.SvgaListRoot;
import com.codder.ultimate.profile.modelclass.VipPlanRoot;
import com.codder.ultimate.reels.activity.ReelsActivity;
import com.codder.ultimate.reels.model.ReliteRoot;
import com.codder.ultimate.reels.utils.MyExoPlayer;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.socket.MySocketManager;
import com.codder.ultimate.utils.NetWorkChangeReceiver;
import com.codder.ultimate.utils.SvgaCacheManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements MyRewardAds.RewardAdListener {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NetWorkChangeReceiver netWorkChangeReceiver;
    private BroadcastReceiver uploadProgressReceiver;
    private boolean isUploadingLytShown = false;
    private MyRewardAds myRewardAds;
    private ExecutorService executorService;
    private ScreenSlidePagerAdapter screenSlidePagerAdapter;
    private int totalCategories;
    private int currentIndex;

    public static int position;
    private volatile boolean isActivityInForeground = false;

    private final List<GiftRoot.GiftItem> allGiftList = new ArrayList<>();
    private void runIfActive(Runnable task) {
        if (!isActivityInForeground || isFinishing() || isDestroyed()) return;
        runOnUiThread(task);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainApplication.isAppOpen = true;
        isActivityInForeground = true;
        if (binding.shimmer != null && binding.shimmer.getVisibility() == View.VISIBLE) {
            binding.shimmer.startShimmer();
        }

        // Register receiver for upload progress
        setupProgressReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.UPLOAD_PROGRESS);
        filter.addAction(Const.PROGRESS_DONE);
        filter.addAction(Const.UPLOAD_SUCCESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(uploadProgressReceiver, filter);

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    LiveHandler liveHandler = new LiveHandler() {

        @Override
        public void onGift(Object[] args) {

        }

        @Override
        public void onComment(Object[] args) {

        }

        @Override
        public void onView(Object[] args) {

        }

        @Override
        public void onBlock(Object[] args) {

        }

        @Override
        public void onAnimationFilter(Object[] args) {

        }

        @Override
        public void onSimpleFilter(Object[] args) {

        }

        @Override
        public void onGif(Object[] args) {

        }

        @Override
        public void onLiveEndByEnd(Object[] args) {

        }

        @Override
        public void onPkRequest(Object[] args) {

        }

        @Override
        public void onPkRequestAnswer(Object[] args) {

        }

        @Override
        public void onPkEnd(Object[] args) {

        }

        @Override
        public void onHostLiveEnd(Object[] args) {

        }

        @Override
        public void onSingleLiveUser(Object[] args) {

        }

        @Override
        public void onGetUser(Object[] args) {

        }

        @Override
        public void onUserCoinUpdate(Object[] args) {

        }

        @Override
        public void onBlockUserAlert(Object[] args) {

        }

        @Override
        public void onBanned(Object[] args) {

        }

        @Override
        public void onBannedUserList(Object[] args) {

        }

        @Override
        public void onTotalRoomCoins(Object[] args) {

        }

        @Override
        public void onHostDetailsForAudience(Object[] args) {

        }

        @Override
        public void onGame(Object[] args) {
            runIfActive(() -> {
                Log.d(TAG, "ongame: =======main");
                handleNotification(args, "onGame");
            });
        }

        @Override
        public void onBroadcastNotification(Object[] args) {
            runIfActive(() -> {
                Log.d(TAG, "onBrodcastNotification: =====main");
                handleNotification(args, "onBrodcastNotification");
            });
        }
    };

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
            Log.w(TAG, "Unknown eventType: " + eventType);

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

            if ("onGame".equals(eventType)) {
                // Handle onGame JSON structure
                name = jsonObject.getString("message");
                imageUrl = jsonObject.getString("userImage");

                binding.icGiftImage.setVisibility(GONE);
                // Load user image
                Glide.with(MainActivity.this).load(imageUrl).placeholder(R.drawable.profile_placeholder).into(binding.ivGameUserImage);

                binding.tvGameNotification.setText(name);

                // Load random banner image if available
                List<BroadcastBannerRoot.BroadcastBannerItem> bannerItemList = sessionManager.getGameBroadcastBannerList();
                if (bannerItemList != null && !bannerItemList.isEmpty()) {
                    String randomBannerUrl = getRandomBannerImage(bannerItemList);

                    Glide.with(MainActivity.this).load(BuildConfig.BASE_URL + randomBannerUrl).into(new CustomTarget<Drawable>() {
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
                Glide.with(MainActivity.this).load(imageUrl).placeholder(R.drawable.profile_placeholder).into(binding.ivUserImage);

                binding.tvNotification.setText(name);

                Glide.with(MainActivity.this)
                        .load(giftUrl)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .into(binding.icGiftImage);

                // Load random banner image if available
                List<BroadcastBannerRoot.BroadcastBannerItem> bannerItemList = sessionManager.getBroadcastBannerList();
                if (bannerItemList != null && !bannerItemList.isEmpty()) {
                    String randomBannerUrl = getRandomBannerImage(bannerItemList);

                    Glide.with(MainActivity.this).load(BuildConfig.BASE_URL + randomBannerUrl).into(new CustomTarget<Drawable>() {
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
        binding.lytNotification.setVisibility(VISIBLE);

        Animation slideIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_slide_right_to_left);
        binding.lytNotification.startAnimation(slideIn);

        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Animation slideOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_right_to_left);
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

        Animation slideIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_slide_right_to_left);
        binding.lytGameNotification.startAnimation(slideIn);

        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Animation slideOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_right_to_left);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        MySocketManager.getInstance().addLiveHandler(liveHandler);
        // Init socket connection if not connected
        if (!MySocketManager.getInstance().globalConnecting || !MySocketManager.getInstance().globalConnected) {
            getApp().initGlobalSocket();
        }
        Log.e(TAG, "onCreate: globalConnecting" + MySocketManager.getInstance().globalConnecting);
        Log.e(TAG, "onCreate: globalConnected" + MySocketManager.getInstance().globalConnected);

        // Start shimmer (loading UI)
        binding.mainLayout.setVisibility(View.INVISIBLE);
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.shimmer.startShimmer();

        myRewardAds = new MyRewardAds(this, this);

        initMain();
        getAdsKeys();
        getStickers();
        getGiftCategory();  // fetch categories & preload gifts
        getVipPlan();
        getBackgroundTheme();
        getFrame();
        getEntry();
        getBannerList();
        getGameBannerList();
    }

    // -------------------- Gift Handling -----------------------
    public void getGiftCategory() {
        Call<GiftCategoryRoot> call = RetrofitBuilder.create().getGiftCategory();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GiftCategoryRoot> call, Response<GiftCategoryRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GiftCategoryRoot giftCategoryRoot = response.body();

                    if (giftCategoryRoot.isStatus() && giftCategoryRoot.getCategory() != null && !giftCategoryRoot.getCategory().isEmpty()) {
                        // Save categories in session
                        sessionManager.saveGiftCategories(giftCategoryRoot.getCategory());
                        totalCategories = giftCategoryRoot.getCategory().size();
                        currentIndex = 0;
                        // Save categories in session
                        fetchNextGiftList(currentIndex, totalCategories, giftCategoryRoot.getCategory());

                        Log.d(TAG, "onResponse: Gift categories saved successfully. Total categories: " + totalCategories);
                        Log.d(TAG, "onResponse: Categories: " + sessionManager.getGiftCategoriesList().toString());
                    } else {
                        Log.w(TAG, "onResponse: No categories found or status is false.");
                    }
                } else {
                    Log.e(TAG, "onResponse: Failed to fetch gift categories. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GiftCategoryRoot> call, Throwable t) {
                Log.e(TAG, "onFailure: Error fetching gift categories", t);
            }
        });
    }

    private void fetchNextGiftList(final int currentIndex, final int totalCategories, List<GiftCategoryRoot.CategoryItem> category) {
        // Recursively fetch gifts category by category
        if (currentIndex >= totalCategories) {
            // All requests are done
            return;
        }
        Log.d(TAG, "fetchNextGiftList: currentIndex ==== " + currentIndex);
        String categoryId = category.get(currentIndex).getId();
        getGiftsList(categoryId, currentIndex, new GiftListCallback() {
            @Override
            public void onGiftListFetched(int currentIndex) {
                // Gift list for the current category fetched

                // Continue to the next category
                fetchNextGiftList(currentIndex + 1, totalCategories, category);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "onError: " + errorMessage);
            }
        });
    }

    private void getGiftsList(String id, int currentIndex, final GiftListCallback callback) {
        Call<GiftRoot> call = RetrofitBuilder.create().getGiftsByCategory(id);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GiftRoot> call, Response<GiftRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GiftRoot giftRoot = response.body();

                    if (giftRoot.isStatus() && giftRoot.getGift() != null && !giftRoot.getGift().isEmpty()) {
                        List<GiftRoot.GiftItem> gifts = giftRoot.getGift();
                        String category = gifts.get(0).getCategory();
                        allGiftList.addAll(gifts);
                        // Save gifts in session
                        sessionManager.saveGiftsList(category, gifts);
                        preloadGiftImages(gifts);  // Save gifts in session
                        Log.d(TAG, "onResponse: Gifts list fetched successfully for category: " + category);

                        for (GiftRoot.GiftItem gift : gifts) {
                            if (gift.getType() == 2) {
                                String svgaUrl = BuildConfig.BASE_URL + gift.getImage();
                                ExecutorService executor = Executors.newSingleThreadExecutor();
                                executor.execute(() -> {
                                    SvgaCacheManager.downloadAndCacheSvga(svgaUrl, MainActivity.this);
                                    Log.d(TAG, "onResponse: SVGA download started for: " + svgaUrl);
                                });
                            }
                        }

                        // Notify callback
                        callback.onGiftListFetched(currentIndex);
                    } else {
                        Log.w(TAG, "onResponse: No gifts found or status is false.");
                        callback.onError("No gifts found for this category.");
                    }
                } else {
                    Log.w(TAG, "onResponse: Failed to fetch gifts, code: " + response.code());
                    callback.onError("Failed to fetch gifts, code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GiftRoot> call, Throwable t) {
                Log.e(TAG, "onFailure: Error fetching gift list", t);
                callback.onError(t.getMessage());
            }
        });
    }

    private void preloadGiftImages(List<GiftRoot.GiftItem> gifts) {
        // Preload SVGA & image gifts into cache
        for (GiftRoot.GiftItem gift : gifts) {
            if (gift.getType() == 2) {
                // SVGA: download the .svga binary into your own cache
                String svgaUrl = BuildConfig.BASE_URL + gift.getSvgaImage(); // ensure correct field
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    SvgaCacheManager.downloadAndCacheSvga(svgaUrl, MainActivity.this);
                    Log.d(TAG, "SVGA prefetch queued: " + svgaUrl);
                });
            } else {
                // Static image: let Glide warm the disk & memory cache
                String imageUrl = BuildConfig.BASE_URL + gift.getImage();

                if (!isFinishing() && !isDestroyed()) {
                    Glide.with(this)
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .preload();
                }
            }
        }
    }

    public void initMain() {
        startNetworkReceiver();
        setupProgressReceiver();
        handleBranchData();
        setupViewPager();
        setupBottomNavigation();
        makeOnlineUser();
        checkUserPlan();

        binding.ivLive.setOnClickListener(view -> {
            startActivity(new Intent(this, GotoLiveActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
        });

        revealMainContentWhenReady(); // Fade in main content after loading
    }

    private void handleBranchData() {
        Intent intent = getIntent();
        String branchData = intent.getStringExtra(Const.DATA);
        String type = intent.getStringExtra(Const.TYPE);
        String userID = intent.getStringExtra("userId");
        String postID = intent.getStringExtra("postId");
        Log.d(TAG, "handleBranchData: =====" + type);

        if (type == null || type.isEmpty()) {
            return;
        }

        if (type.equals("POST")) {

            startActivity(new Intent(this, FeedListActivity.class)
                    .putExtra("userId",userID)
                    .putExtra("postId",postID));

        } else if (type.equals("RELITE")) {

            startActivity(new Intent(this, ReelsActivity.class).putExtra("userId",userID)
                    .putExtra("postId",postID));

        } else if (type.equals("PROFILE")) {

            String userId = branchData;
            startActivity(new Intent(this, GuestActivity.class).putExtra(Const.USERID, userId));

        } else if (type.equals("AUDIO_LIVE")) {
            Log.d(TAG, "handleBranchData: ====" + branchData);

            PkAudioLiveUserRoot.UsersItem usersItem = new Gson().fromJson(branchData, PkAudioLiveUserRoot.UsersItem.class);
            startActivity(new Intent(this, WatchAudioLiveActivity.class).putExtra(Const.DATA, new Gson().toJson(usersItem)));
        } else if (type.equals("FAKE_AUDIO_LIVE")) {
            PkAudioLiveUserRoot.UsersItem randomUser = getRandomFakeUser();

            if (randomUser != null) {
                startActivity(new Intent(this, FakeAudioWatchActivity.class)
                        .putExtra(Const.DATA, new Gson().toJson(randomUser)));
            } else {
                Log.e(TAG, "Fake live list empty");
            }
        }
    }

    public void makeOnlineUser() {
        if (sessionManager.getBooleanValue(Const.IS_LOGIN)) {
            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("userId", sessionManager.getUser().getId());
                Call<RestResponse> call = RetrofitBuilder.create().makeOnlineUser(jsonObject);
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            RestResponse responseBody = response.body();
                            if (responseBody.isStatus()) {
                                Log.d(TAG, "onResponse: User is now online.");
                            } else {
                                Log.w(TAG, "onResponse: User status is false.");
                            }
                        } else {
                            Log.e(TAG, "onResponse: Failed to mark user online. Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<RestResponse> call, Throwable t) {
                        Log.e(TAG, "onFailure: Error making user online", t);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception while making user online", e);
            }
        }
    }

    private void setupViewPager() {
        // Lock swipe navigation, only nav via bottom menu
        screenSlidePagerAdapter = new ScreenSlidePagerAdapter(this);
        binding.viewpagerMain.setAdapter(screenSlidePagerAdapter);
        binding.viewpagerMain.setUserInputEnabled(false);
    }

    private void setupBottomNavigation() {
        if (binding.bottomNavigationView != null) {
            binding.bottomNavigationView.setItemIconTintList(null);
            binding.bottomNavigationView.setSelectedItemId(R.id.miHome);

            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.miHome) {
                    position = 0;
                    setViewPagerItem(0);
                    return true;
                } else if (itemId == R.id.miFeed) {
                    position = 1;
                    setViewPagerItem(1);
                    return true;
                } else if (itemId == R.id.miMessage) {
                    position = 2;
                    setViewPagerItem(2);
                    return true;
                } else if (itemId == R.id.miProfile) {
                    position = 3;
                    setViewPagerItem(3);
                    return true;
                }

                return false;
            });
        }

    }

    private void setViewPagerItem(int position) {
        Log.d(TAG, "Setting ViewPager position to: " + position);
        if (binding.viewpagerMain != null) {
            binding.viewpagerMain.setCurrentItem(position, false);
        }
    }

    private void setupProgressReceiver() {
        uploadProgressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (Const.UPLOAD_PROGRESS.equals(action)) {
                    int progress = intent.getIntExtra("progress", 0);
                    Log.d(TAG, "Upload progress: " + progress + "%");

                    if (!isUploadingLytShown && binding.uploadingImageLyt != null) {
                        isUploadingLytShown = true;
                        binding.uploadingImageLyt.setVisibility(View.VISIBLE);
                        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down);
                        binding.uploadingImageLyt.startAnimation(animation);
                    }

                    if (binding.uploadingProgress != null && binding.progressPercentage != null) {
                        binding.uploadingProgress.setProgress(progress);
                        binding.progressPercentage.setText(progress + "%");
                    }

                } else if (Const.PROGRESS_DONE.equals(action)) {
                    Log.d(TAG, "Upload complete. Hiding progress UI.");
                    if (binding.uploadingImageLyt != null) {
                        isUploadingLytShown = false;
                        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up);
                        binding.uploadingImageLyt.startAnimation(animation);
                        binding.uploadingImageLyt.setVisibility(View.GONE);
                    }
                } else if (Const.UPLOAD_SUCCESS.equals(action)) {
                    Log.d(TAG, "Upload successful. Show success message or update UI here.");
                    Toast.makeText(MainActivity.this, getString(R.string.upload_completed), Toast.LENGTH_SHORT).show();
                }

            }
        };
    }


    private void startNetworkReceiver() {
        // Show/hide internet status banner
        netWorkChangeReceiver = new NetWorkChangeReceiver(this::updateInternetUI);
        registerReceiver(netWorkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void updateInternetUI(Boolean isOnline) {
        TextView tvInternetStatus = findViewById(R.id.tv_internet_status);
        if (tvInternetStatus != null) {
            if (isOnline) {
                if (tvInternetStatus.getVisibility() == View.VISIBLE) {
                    tvInternetStatus.setText(R.string.back_online);
                    tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                    new Handler().postDelayed(() -> slideToTop(tvInternetStatus), 300);
                }
            } else {
                tvInternetStatus.setText(R.string.no_internet_connection);
                tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
                if (tvInternetStatus.getVisibility() == View.GONE) {
                    slideToBottom(tvInternetStatus);
                }
            }
        }
    }

    private void slideToTop(View view) {
        if (view != null) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter_up);
            view.startAnimation(animation);
            view.setVisibility(View.GONE);
        }
    }

    private void slideToBottom(View view) {
        if (view != null) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter_down);
            view.startAnimation(animation);
            view.setVisibility(View.VISIBLE);
        }
    }

    private void checkUserPlan() {
        if (sessionManager.getUser() != null) {
            Call<RestResponse> call = RetrofitBuilder.create().checkUserPlan(sessionManager.getUser().getId());
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        RestResponse responseBody = response.body();
                        if (responseBody.isStatus()) {
                            Log.d(TAG, "User plan is valid: " + responseBody.getMessage());
                        } else {
                            Log.w(TAG, "User plan is not valid: " + responseBody.getMessage());
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch user plan. Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<RestResponse> call, Throwable t) {
                    Log.e(TAG, "Error checking user plan", t);
                }
            });
        }
    }

    public interface GiftListCallback {
        void onGiftListFetched(int currentIndex);

        void onError(String errorMessage);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Force stop ExoPlayer if not in Relites tab
        if (binding.viewpagerMain.getCurrentItem() != 2) {
            MyExoPlayer.getInstance().stopAndReleasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (uploadProgressReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(uploadProgressReceiver);
            if (binding.shimmer != null) binding.shimmer.stopShimmer();
        }
        isActivityInForeground = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (uploadProgressReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(uploadProgressReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(netWorkChangeReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MySocketManager.getInstance().removeLiveHandler(liveHandler);
        MainApplication.isAppOpen = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (binding.viewpagerMain != null && binding.viewpagerMain.getCurrentItem() == 0) {
            new PopupBuilder(this).showExitPopup(super::onBackPressed);
        } else if (binding.viewpagerMain != null) {
            setViewPagerItem(0);
            if (binding.bottomNavigationView != null) {
                binding.bottomNavigationView.setSelectedItemId(R.id.miHome);
            }
        }
    }

    @Override
    public void onAdClosed() {

    }

    @Override
    public void onEarned() {

    }

    public void getVipPlan() {
        Call<VipPlanRoot> call = RetrofitBuilder.create().getVipPlan();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<VipPlanRoot> call, Response<VipPlanRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VipPlanRoot root = response.body();

                    if (root.isStatus() && root.getVipPlan() != null && !root.getVipPlan().isEmpty()) {
                        sessionManager.saveVipPlan(response.body().getVipPlan());
                    } else {
                        Log.e(TAG, "Unsuccessful response or null body");
                    }
                }
            }

            @Override
            public void onFailure(Call<VipPlanRoot> call, Throwable t) {
                Log.e(TAG, "Failed to fetch VIP Plan", t);
            }
        });

    }

    private void getBackgroundTheme() {
        Call<ThemeRoot> call = RetrofitBuilder.create().getTheme();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ThemeRoot> call, Response<ThemeRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ThemeRoot themeRoot = response.body();
                    List<ThemeRoot.ThemeItem> themeList = themeRoot.getTheme();

                    if (themeList == null || themeList.isEmpty()) {
                        Log.w(TAG, "No themes found in the response.");
                        return;
                    }

                    for (ThemeRoot.ThemeItem item : themeList) {
                        if (item.isDefault()) {
                            String defaultThemePath = item.getTheme();
                            Log.d(TAG, "Default theme saved: " + defaultThemePath);

                            sessionManager.saveStringValue("isDefaultBackground", defaultThemePath);
                            break;
                        }
                    }

                    Log.w(TAG, "No default theme found in the response.");
                } else {
                    Log.e(TAG, "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ThemeRoot> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    private void revealMainContentWhenReady() {
        final View root = binding.getRoot();
        root.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                root.getViewTreeObserver().removeOnPreDrawListener(this);

                binding.mainLayout.setAlpha(0f);
                binding.mainLayout.setVisibility(View.VISIBLE);
                binding.mainLayout.animate().alpha(1f).setDuration(0).withEndAction(() -> {
                    binding.shimmer.animate().alpha(0f).setDuration(0).withEndAction(() -> {
                        binding.shimmer.stopShimmer();
                        binding.shimmer.setVisibility(View.GONE);
                        binding.shimmer.setAlpha(1f);
                        // 👉 UI is fully visible now; show the privacy popup
                        maybeShowPrivacyPopupAfterUi();
                    }).start();
                }).start();

                getWindow().setBackgroundDrawable(null);
                return true;
            }
        });
    }

    private void maybeShowPrivacyPopupAfterUi() {
        if (!sessionManager.getBooleanValue(Const.POLICY_ACCEPTED)) {
            new PopupBuilder(this).PrivacyPopup(new PopupBuilder.OnSubmitClickListener() {
                @Override
                public void onAccept() {
                    sessionManager.saveBooleanValue(Const.POLICY_ACCEPTED, true);
                    maybeRequestPermissionsNow();  // ask only after accept
                }

                @Override
                public void onDeny() {
                    finishAffinity();
                }
            });
        } else {
            // Already accepted in a past run; optionally request any still-missing permissions now
            maybeRequestPermissionsNow();
        }
    }

    private void maybeRequestPermissionsNow() {
        // Request camera & mic (mandatory), notifications (optional for Android 13+)
        List<String> permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
                : Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);

        requestPermissionIfNeeded(permissions, (allGranted, grantedList, deniedList) -> {
            // Core requirements:
            boolean cameraOk = grantedList.contains(Manifest.permission.CAMERA)
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;

            boolean micOk = grantedList.contains(Manifest.permission.RECORD_AUDIO)
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;

            // Optional on Android 13+: notifications
            boolean notifDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && deniedList.contains(Manifest.permission.POST_NOTIFICATIONS);

            if (cameraOk && micOk) {
                Log.d(TAG, "checkPermissionAndInitialize: if " + cameraOk);
                Log.d(TAG, "maybeRequestPermissionsNow: " + sessionManager.isNotificationOn());
                // (Optional) gently inform the user they won’t get alerts
                if (notifDenied) {
                    sessionManager.notificationOnOff(false);
                } else {
                    sessionManager.notificationOnOff(true);
                }

            } else {
                Log.d(TAG, "checkPermissionAndInitialize: else ");
                // core call permissions missing → exit or re-prompt
                finishAffinity(); // Exit if core permissions denied
            }
        });
    }

    private void getEntry() {
        if (sessionManager.getUser() != null && sessionManager.getUser().getId() != null) {
            Call<SvgaListRoot> call = RetrofitBuilder.create().getSvgaList(sessionManager.getUser().getId(), "svga", 0, Integer.MAX_VALUE);
            call.enqueue(new Callback<SvgaListRoot>() {
                @Override
                public void onResponse(Call<SvgaListRoot> call, Response<SvgaListRoot> response) {
                    if (response.code() == 200) {
                        if (response.body().isStatus() && response.body().getData() != null) {
                            List<SvgaListRoot.DataItem> gifts = response.body().getData();

                            List<String> svgaUrls = new ArrayList<>();

                            for (SvgaListRoot.DataItem gift : gifts) {

                                String svgaUrl = BuildConfig.BASE_URL + gift.getImage();

                                svgaUrls.add(svgaUrl);

                                executorService = Executors.newSingleThreadExecutor();
                                executorService.execute(() -> {
                                    // This runs in a background thread
                                    SvgaCacheManager.downloadAndCacheSvga(svgaUrl, MainActivity.this);
                                    // Then post back to main thread if you need to update UI
                                });

                                //For fake data
                                SvgaEntryManager.setSvgaList(svgaUrls);


                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<SvgaListRoot> call, Throwable t) {
                    Log.d(TAG, "onFailure: SvgaListRoot =====" + t.getMessage());
                }
            });
        }
    }

    private void getFrame() {
        Call<SvgaListRoot> call = RetrofitBuilder.create().getSvgaList(sessionManager.getUser().getId(), "frame", 0, Integer.MAX_VALUE);
        call.enqueue(new Callback<SvgaListRoot>() {
            @Override
            public void onResponse(Call<SvgaListRoot> call, Response<SvgaListRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && response.body().getData() != null) {
                        List<SvgaListRoot.DataItem> gifts = response.body().getData();
                        for (SvgaListRoot.DataItem gift : gifts) {

                            String imageUrl = BuildConfig.BASE_URL + gift.getImage();
                            String extension = gift.getImage().substring(gift.getImage().lastIndexOf('.') + 1).toLowerCase();

                            executorService = Executors.newSingleThreadExecutor();
                            executorService.execute(() -> {
                                switch (extension) {
                                    case "svga":
                                        SvgaCacheManager.downloadAndCacheSvga(imageUrl, MainActivity.this);
                                        break;
                                    case "png":
                                        preloadImage(imageUrl, MainActivity.this);
                                        break;
                                    case "webp":
                                        preloadImage(imageUrl, MainActivity.this);
                                        break;
                                    case "jpeg":
                                        preloadImage(imageUrl, MainActivity.this);
                                        break;
                                    case "jpg":
                                        preloadImage(imageUrl, MainActivity.this);
                                        break;
                                    default:
                                        Log.w("Preload", "Unsupported image format: " + extension);
                                }
                            });


                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SvgaListRoot> call, Throwable t) {
                Log.d(TAG, "onFailure: SvgaListRoot =====" + t.getMessage());
            }
        });
    }

    private void getBannerList() {
        Call<BroadcastBannerRoot> call = RetrofitBuilder.create().getBroadcastBanner(1);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BroadcastBannerRoot> call, @NonNull Response<BroadcastBannerRoot> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                        sessionManager.saveBroadcastBannerList(response.body().getData());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BroadcastBannerRoot> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void getGameBannerList() {
        Call<BroadcastBannerRoot> call = RetrofitBuilder.create().getBroadcastBanner(2);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BroadcastBannerRoot> call, @NonNull Response<BroadcastBannerRoot> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                        sessionManager.saveGameBroadcastBannerList(response.body().getData());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BroadcastBannerRoot> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }




    private PkAudioLiveUserRoot.UsersItem getRandomFakeUser() {

        List<PkAudioLiveUserRoot.UsersItem> fakeUsers = sessionManager.getShuffledFakeLiveList();

        if (fakeUsers == null || fakeUsers.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int index = random.nextInt(fakeUsers.size());

        return fakeUsers.get(index);
    }

    private void preloadImage(String imageUrl, Context context) {
        Glide.with(context)
                .downloadOnly()
                .load(imageUrl)
                .addListener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        Log.e("Preload", "Image preload failed: " + imageUrl);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("Preload", "Image preloaded: " + imageUrl);
                        return false;
                    }
                })
                .preload();

    }

    public void openProfileFragment() {
        setViewPagerItem(4);
        binding.bottomNavigationView.setSelectedItemId(R.id.miProfile);
    }
}
