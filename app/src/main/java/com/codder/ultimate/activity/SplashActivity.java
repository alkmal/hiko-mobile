package com.codder.ultimate.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.databinding.ActivitySplashBinding;
import com.codder.ultimate.launguagetranslation.TranslationManager;
import com.codder.ultimate.launguagetranslation.modelclass.ActiveLanguageRoot;
import com.codder.ultimate.live.model.BannerRoot;
import com.codder.ultimate.live.model.LiveStreamRoot;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.modelclass.IpAddressRoot_e;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.profile.modelclass.SettingRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.socket.MySocketManager;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends BaseActivity {

    ActivitySplashBinding binding;

    private static final String TAG = "SplashActivity";
    SessionManager sessionManager;
    private String branchData = "";
    private String type = "",userId,postId;
    private boolean hasNavigated = false;

    private long startTimeMillis;


    @Override

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        sessionManager = new SessionManager(this);
        fetchAndApplyDefaultLanguage(() -> {

            ((TextView) findViewById(R.id.tvVersion)).setText(getString(R.string.version_) + BuildConfig.VERSION_CODE);

            GlideBuilder builder = new GlideBuilder();
            builder.setMemoryCache(new LruResourceCache(20 * 1024 * 1024))
                    .setDiskCache(new InternalCacheDiskCacheFactory(this, 100 * 1024 * 1024));
            Glide.init(this, builder);

            if (!MySocketManager.getInstance().globalConnecting || !MySocketManager.getInstance().globalConnected) {
                getApp().initGlobalSocket();
            }

            // For Deep Link
            Intent intent = getIntent();
            Uri data = intent.getData();

            if (data != null) {
                Log.d(TAG, "Deep Link: " + data.toString());

                if ("enter your domain".equals(data.getHost())) {

                    String userID = data.getQueryParameter("userId");
                    String joinUserId = data.getQueryParameter("joinUserId");
                    String liveStreamingId = data.getQueryParameter("liveStreamingId");
                    String livetype = data.getQueryParameter("livetype");
                    String postID = data.getQueryParameter("postId");

                    if (livetype != null) {
                        singleLiveUserEventFire(userID, joinUserId, liveStreamingId, livetype);
                    }
                    userId = userID;
                    postId = postID;

                    type = data.getQueryParameter("type"); // <-- added

                    Log.d(TAG, "Deeplink Type: " + type);


                }
            }

        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        startTimeMillis = System.currentTimeMillis();



        MobileAds.initialize(this, initializationStatus -> {
        });
        FirebaseMessaging.getInstance().subscribeToTopic("CHAPI");

        checkNetwork(false);

        applyGradientToTextView(binding.tvAppname,true);

        preloadGameImages();
        getBanner();
        getFakeLiveList();
        getIp();
    }

    private void preloadGameImages() {
        if (sessionManager != null && sessionManager.getSetting() != null && sessionManager.getSetting().getGame() != null) {
            List<String> gameImageUrls = new ArrayList<>();

            // Iterate over the game list and preload each image URL
            for (int i = 0; i < sessionManager.getSetting().getGame().size(); i++) {
                String gameImageUrl = sessionManager.getSetting().getGame().get(i).getImage();
                if (gameImageUrl != null && !gameImageUrl.isEmpty()) {
                    gameImageUrls.add(gameImageUrl);
                }
            }

            // Preload images using Glide with logging
            for (String imageUrl : gameImageUrls) {
                Glide.with(this)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                // Log if preload failed
                                Log.e("GlidePreload", "Failed to preload image: " + imageUrl);
                                return false;  // Return false to allow Glide's default error handling
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                // Log when preload is successful
                                Log.d("GlidePreload", "Successfully preloaded image: " + imageUrl);
                                return false;  // Return false to allow Glide's default resource handling
                            }
                        })
                        .preload();
            }
        }
    }

    private void checkNetwork(boolean forceIpCall) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean isConnected = mobile != null && mobile.isConnectedOrConnecting();
        boolean isConnected2 = wifi != null && wifi.isConnectedOrConnecting();

        showHideInternet(isConnected || isConnected2, forceIpCall);
    }

    private void showHideInternet(Boolean isOnline, boolean forceIpCall) {
        final TextView tvInternetStatus = findViewById(R.id.tv_internet_status);

        if (isOnline) {
            if (forceIpCall) getIp();
            if (tvInternetStatus != null && tvInternetStatus.getVisibility() == View.VISIBLE
                    && tvInternetStatus.getText().toString().equalsIgnoreCase(getString(R.string.no_internet_connection))) {
                tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                tvInternetStatus.setText(R.string.back_online);
                new Handler().postDelayed(() -> {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter_up);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tvInternetStatus.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    tvInternetStatus.startAnimation(animation);
                }, 200);
            }
        } else {
            if (tvInternetStatus != null) {
                tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
                tvInternetStatus.setText(R.string.no_internet_connection);
                if (tvInternetStatus.getVisibility() == View.GONE) {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter_down);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tvInternetStatus.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    tvInternetStatus.startAnimation(animation);
                }
            }
        }
    }


    private void gotoMainPage() {
        if (hasNavigated) return;
        hasNavigated = true;

        long elapsed = System.currentTimeMillis() - startTimeMillis;
        Log.d(TAG, "Time elapsed before navigating to main page: " + elapsed + " ms");

        new Handler(Looper.myLooper()).postDelayed(() -> {
            if (sessionManager.getBooleanValue(Const.IS_LOGIN)) {
                UserApiCall userApiCall = new UserApiCall(this);
                userApiCall.getUser(new UserApiCall.OnUserApiListener() {
                    @Override
                    public void onUserGot(UserRoot.User user) {
                        long userReceivedTime = System.currentTimeMillis();
                        Log.d(TAG, "Time elapsed until user data received: " + (userReceivedTime - startTimeMillis) + " ms");

                        if (user.isIsBlock()) {
                            new PopupBuilder(SplashActivity.this).showSimplePopup(getString(R.string.you_are_blocked_by_admin_text), getString(R.string.dismiss), () -> finishAffinity());
                        } else {
                            checkUser(user);
                        }
                    }

                    @Override
                    public void onUserStatusFailed(String message) {
                        long failedTime = System.currentTimeMillis();
                        Log.d(TAG, "Time elapsed until user status failed: " + (failedTime - startTimeMillis) + " ms");

                        if (message.contains("User does not Exist")) {
                            Toast.makeText(SplashActivity.this, message, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            new PopupBuilder(SplashActivity.this).showSimplePopup(getString(R.string.you_are_blocked_by_admin_text), getString(R.string.cancel), () -> finishAffinity());
                        }
                    }
                });

            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
        }, 500);
    }

    private void checkUser(UserRoot.User user) {
        long navigationStart = System.currentTimeMillis();

        String localIdentity = sessionManager.getUser() != null ? sessionManager.getUser().getIdentity() : null;
        String remoteIdentity = user != null ? user.getIdentity() : null;
        Log.d(TAG, "checkUser: local Id " + localIdentity);
        Log.d(TAG, "checkUser: remote Id " + remoteIdentity);
        if (TextUtils.isEmpty(remoteIdentity) || TextUtils.isEmpty(localIdentity) || remoteIdentity.equals(localIdentity)) {
            sessionManager.saveUser(user);
            checkHostLiveOrNot();

            Log.d(TAG, "Navigating to MainActivity after " + (navigationStart - startTimeMillis) + " ms from splash start");
            Log.d(TAG, "checkUser: ======" + branchData);
            startActivity(new Intent(SplashActivity.this, MainActivity.class)
                    .putExtra(Const.DATA, branchData).putExtra(Const.TYPE, type).putExtra("userId",userId).putExtra("postId",postId));

        } else {
            Log.d(TAG, "User logged in on other device, navigating to LoginActivity after " + (navigationStart - startTimeMillis) + " ms");

            new PopupBuilder(this).showSimplePopup(getString(R.string.you_are_logged_in_other_devices), getString(R.string.dismiss), () -> {
                GoogleSignInOptions gso = new GoogleSignInOptions.
                        Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                        build();

                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                googleSignInClient.signOut();

                Toast.makeText(this, getString(R.string.log_out), Toast.LENGTH_SHORT).show();

                sessionManager.saveUser(null);
                sessionManager.saveBooleanValue(Const.IS_LOGIN, false);
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();

            });
        }

    }

    public void getBanner() {

        Call<BannerRoot> call = RetrofitBuilder.create().getBanner("hello");
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BannerRoot> call, Response<BannerRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BannerRoot bannerRoot = response.body();
                    if (bannerRoot.isStatus() && bannerRoot.getBanner() != null && !bannerRoot.getBanner().isEmpty()) {
                        sessionManager.saveBannerList(bannerRoot.getBanner());
                    } else {
                        Log.d(TAG, "No banners found or invalid response.");
                    }
                } else {
                    Log.d(TAG, "Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<BannerRoot> call, Throwable t) {
                Log.e(TAG, "Request failed", t);
            }
        });

    }

    private void getFakeLiveList() {
        Call<PkAudioLiveUserRoot> call = RetrofitBuilder.create().getFakeLiveList(0, Const.LIMIT30);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PkAudioLiveUserRoot> call, @NonNull Response<PkAudioLiveUserRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        if (response.body().getUsers() != null && !response.body().getUsers().isEmpty()) {
                            sessionManager.saveFakeLiveList(response.body().getUsers());
                        } else {
                            sessionManager.saveFakeLiveList(new ArrayList<>());
                        }
                    }else {
                        Log.d(TAG, "Response status not successful.");
                    }
                }else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PkAudioLiveUserRoot> call, Throwable t) {
                Log.e(TAG, "Request failed", t);
            }
        });
    }

    private void getIp() {
        Call<IpAddressRoot_e> call = RetrofitBuilder.getIp().getIp();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<IpAddressRoot_e> call, Response<IpAddressRoot_e> response) {
                if (response.code() == 200 && response.body() != null) {
                    IpAddressRoot_e ipData = response.body();
                    Log.d(TAG, "onResponse: =====ip data : " + ipData);

                    if (ipData.getCountry() != null) {
                        Log.d(TAG, "IP Info retrieved: " + ipData.getQuery());

                        sessionManager.saveStringValue(Const.COUNTRY, response.body().getCountry());
                        sessionManager.saveStringValue(Const.CURRENT_CITY, response.body().getCity());
                        sessionManager.saveStringValue(Const.COUNTRY_CODE, response.body().getCountryCode());

                        if (ipData.getQuery() != null) {
                            sessionManager.saveStringValue(Const.IPADDRESS, response.body().getQuery());
                        }
                        getSetting();
                    } else {
                        Log.w(TAG, "IP response does not contain country info.");
                        getSetting();
                    }
                } else {
                    Log.e(TAG, "Failed response: Code=" + response.code() + ", Message=" + response.message());
                    getSetting();
                }

            }

            @Override
            public void onFailure(Call<IpAddressRoot_e> call, Throwable t) {
                Log.e(TAG, "IP API call failed: " + t.getMessage(), t);
                getSetting();

            }
        });
    }


    private void getSetting() {
        Log.d(TAG, "getSetting: Starting API call to fetch settings");

        Call<SettingRoot> call = RetrofitBuilder.create().getSettings();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SettingRoot> call, Response<SettingRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SettingRoot settingRoot = response.body();


                    if (settingRoot.isStatus() && settingRoot.getSetting() != null) {
                        sessionManager.saveSetting(settingRoot.getSetting());
                        ((MainApplication) getApplication()).initAgora(SplashActivity.this);
                        Const.setCurrency(sessionManager.getSetting().getCurrency().getSymbol());

                        if (sessionManager.getSetting().isIsAppActive()) {
                            gotoMainPage();
                        } else {
                            new PopupBuilder(SplashActivity.this)
                                    .showSimplePopup(
                                            getString(R.string.we_are_under_maintenance_text),
                                            getString(R.string.dismiss), () -> finishAffinity());
                        }
                    } else {
                        Log.w(TAG, "Settings status false or missing settings data.");
                    }
                } else {
                    Log.e(TAG, "getSettings failed: HTTP " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SettingRoot> call, Throwable t) {
                Log.e(TAG, "getSettings onFailure: " + t.getMessage(), t);
            }
        });

    }

    private void checkHostLiveOrNot() {
        if (sessionManager.getUser() != null) {
            Call<LiveStreamRoot> call = RetrofitBuilder.create().checkUserLiveOrNot(sessionManager.getUser().getId());
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<LiveStreamRoot> call, Response<LiveStreamRoot> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isStatus()) {
                            sessionManager.setIsAudioRoomBackground(true);
                            String data = new Gson().toJson(response.body().getLiveUser());
                            Log.d(TAG, "onResponse: ========" + data);
                            sessionManager.saveLiveUserForBackground(new Gson().fromJson(data, PkAudioLiveUserRoot.UsersItem.class));
                        } else {
                            sessionManager.setIsAudioRoomBackground(false);
                        }
                    }else {
                        Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<LiveStreamRoot> call, Throwable t) {
                    Log.e(TAG, "Request failed", t);
                }

            });
        }
    }


    private void singleLiveUserEventFire(String userId,String joinUserId,String livestreamingId,String type) {

        try {
            JSONObject json = new JSONObject();
            json.put("userId", userId);
            json.put("joinUserId", joinUserId);
            json.put("liveStreamingId", livestreamingId);
            json.put("type",type);
//            if (MySocketManager.getInstance().globalConnecting) {
                MySocketManager.getInstance().getSocket().emit("singleLiveUser", json);
//            }

            Log.d(TAG, "singleLiveUserEventFire: ======" + json);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for socket event", e);
        }

        MySocketManager.getInstance().getSocket().on(Const.DUMMY, args -> {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Log.d(TAG, "DUMMY event received: ==> " + args[0].toString());
                    branchData = args[0].toString();

                }
            });
        });

    }


//    private void fetchAndApplyDefaultLanguage(Runnable onComplete) {
//
//        if (sessionManager == null) sessionManager = new SessionManager(this);
//
//        String savedCode = sessionManager.getStringValue(Const.SELECTED_LANGUAGE);
//
//        if (!TextUtils.isEmpty(savedCode)) {
//            // Already saved → directly translations fetch કરો
//            TranslationManager.getInstance()
//                    .checkAndUpdateGlobalVersion(this, () ->
//                            // Global version check pachhi translation fetch
//                            TranslationManager.getInstance()
//                                    .fetchTranslations(this, savedCode, onComplete)
//                    );
//            return;
//        }
//
//        // પહેલી વાર → API default language fetch કરો
//        Call<ActiveLanguageRoot> call = RetrofitBuilder.create().fetchLanguage(1, 10);
//        call.enqueue(new Callback<ActiveLanguageRoot>() {
//            @Override
//            public void onResponse(Call<ActiveLanguageRoot> call,
//                                   Response<ActiveLanguageRoot> response) {
//
//                if (response.isSuccessful()
//                        && response.body() != null
//                        && response.body().isStatus()
//                        && response.body().getDocs() != null) {
//
//                    for (ActiveLanguageRoot.DocsItem doc : response.body().getDocs()) {
//                        if (doc.isIsDefault()) {
//                            // Save default language
//                            sessionManager.saveStringValue(Const.SELECTED_LANGUAGE, doc.getLanguageCode());
//                            sessionManager.saveStringValue(Const.LANGUAGE, doc.getLanguageTitle());
//
//                            // Locale apply
//                            Locale locale = new Locale(doc.getLanguageCode());
//                            Locale.setDefault(locale);
//                            Configuration config = new Configuration();
//                            config.setLocale(locale);
//                            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
//
//                            // Translations fetch → પછી onComplete
//                            TranslationManager.getInstance()
//                                    .fetchTranslations(SplashActivity.this, doc.getLanguageCode(), onComplete);
//                            return;
//                        }
//                    }
//                }
//                // Default ન મળ્યો → onComplete directly
//                onComplete.run();
//            }
//
//            @Override
//            public void onFailure(Call<ActiveLanguageRoot> call, Throwable throwable) {
//                onComplete.run();
//            }
//        });
//    }


    private void fetchAndApplyDefaultLanguage(Runnable onComplete) {

        if (sessionManager == null) sessionManager = new SessionManager(this);
        String savedCode = sessionManager.getStringValue(Const.SELECTED_LANGUAGE);

        if (!TextUtils.isEmpty(savedCode)) {

            // ✅ PAHELA global version check karo — PACHHI translation fetch
            TranslationManager.getInstance().checkAndUpdateGlobalVersion(this, () -> {
                TranslationManager.getInstance().fetchTranslations(this, savedCode, onComplete);
            });
            return;
        }

        // First time — default language API thi fetch karo
        Call<ActiveLanguageRoot> call = RetrofitBuilder.create().fetchLanguage(1, 50);
        call.enqueue(new Callback<ActiveLanguageRoot>() {
            @Override
            public void onResponse(Call<ActiveLanguageRoot> call,
                                   Response<ActiveLanguageRoot> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isStatus()
                        && response.body().getDocs() != null) {

                    for (ActiveLanguageRoot.DocsItem doc : response.body().getDocs()) {
                        if (doc.isIsDefault()) {
                            sessionManager.saveStringValue(Const.SELECTED_LANGUAGE, doc.getLanguageCode());
                            sessionManager.saveStringValue(Const.LANGUAGE, doc.getLanguageTitle());

                            Locale locale = new Locale(doc.getLanguageCode());
                            Locale.setDefault(locale);
                            Configuration config = new Configuration();
                            config.setLocale(locale);
                            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

                            // ✅ First time pn global version check karo
                            TranslationManager.getInstance().checkAndUpdateGlobalVersion(
                                    SplashActivity.this, () -> {
                                        TranslationManager.getInstance().fetchTranslations(
                                                SplashActivity.this, doc.getLanguageCode(), onComplete);
                                    }
                            );
                            return;
                        }
                    }
                }
                onComplete.run();
            }

            @Override
            public void onFailure(Call<ActiveLanguageRoot> call, Throwable throwable) {
                onComplete.run();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding.ivLogo != null) {
            binding.ivLogo.clearAnimation();
        }
    }

}
