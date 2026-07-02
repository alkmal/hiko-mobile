package com.codder.ultimate.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.LayoutInflaterCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.agora.rtc.EventHandler;
import com.codder.ultimate.chat.activity.CallIncomeActivity;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.launguagetranslation.TranslationContextWrapper;
import com.codder.ultimate.launguagetranslation.TranslationInflaterFactory;
import com.codder.ultimate.live.model.StickerRoot;
import com.codder.ultimate.live.utils.FloatingButtonService;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.profile.modelclass.AdsRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.socket.CallHandler;
import com.codder.ultimate.socket.MySocketManager;
import com.google.gson.JsonObject;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String TAG = "BaseActivity";

    protected SessionManager sessionManager;
    public static boolean STATUS_VIDEO_CALL = false;
    public static boolean STATUS_LIVE = false;
    protected UserApiCall userApiCall;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private RequestCallback requestCallback;

    public CustomDialogClass customDialogClass;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "cropimage";
    public static boolean isDemoVersion = true; //TODO False after reskin


    private final List<View> autoHide = new ArrayList<>();

    /**
     * Handles socket events for calls (incoming call, confirm, cancel, etc.)
     */
    CallHandler callHandler = new CallHandler() {
        @Override
        public void onCallRequest(Object[] args1) {
            // Triggered when a call request is received via socket
            if (args1 != null) {
                Log.d(TAG, "EVENT_CALL_REQUEST  : " + args1.toString());
                try {

                    JSONObject jsonObject = new JSONObject(args1[0].toString());
                    String userId1 = jsonObject.getString(Const.USERID1);
                    Log.d(TAG, "onCallRequest: ====" + jsonObject.toString());

                    // Check if this user is the target
                    if (userId1.equals(sessionManager.getUser().getId())) {
                        Log.d(TAG, "getGlobalSocket: is In CALl   " + BaseActivity.STATUS_VIDEO_CALL);
                        if (!BaseActivity.STATUS_VIDEO_CALL && !BaseActivity.STATUS_LIVE) {
                            // Not busy → open incoming call screen
                            BaseActivity.STATUS_VIDEO_CALL = true;
                            Log.d(TAG, "getGlobalSocket:call Object " + jsonObject);
                            startActivity(new Intent(BaseActivity.this, CallIncomeActivity.class).putExtra(Const.DATA, jsonObject.toString()));
                        } else {
                            // Already in a call/live
                            Log.d(TAG, "onCallRequest: User on another call");
                        }
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "getGlobalSocket: err " + e.toString());
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onCallConfirm(Object[] args) {

        }

        @Override
        public void onCallAnswer(Object[] args) {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LayoutInflaterCompat.setFactory2(
                getLayoutInflater(),
                new TranslationInflaterFactory(getDelegate(), this)
        );

        super.onCreate(savedInstanceState);

        // Setup status bar color & transparency
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#310A44"));

            // Optional: for dark status bar icons
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Dark icons on light status bar
                View decor = window.getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // For dark icons
            }
        }


        makeStatusBarTransparent();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_base);

        sessionManager = new SessionManager(this);
        userApiCall = new UserApiCall(this);

        // Reusable custom dialog
        customDialogClass = new CustomDialogClass(this, R.style.customStyle);
        customDialogClass.setCancelable(false);
        customDialogClass.setCanceledOnTouchOutside(false);

        // Register socket listener for calls
        MySocketManager.getInstance().addCallHandler(callHandler);
        applyRTLSupport();
    }

    public MainApplication getApp() {
        return ((MainApplication) getApplication());
    }

    /**
     * Request runtime permissions in one call
     */
    public void requestPermissionIfNeeded(List<String> permissions, RequestCallback callback) {
        if (permissions == null || permissions.isEmpty() || callback == null) {
            Log.w("PermissionRequest", "Permissions list or callback is null/empty.");
            return;
        }

        this.requestCallback = callback;

        List<String> permissionsToRequest = new ArrayList<>();
        // Collect denied permissions
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        // Already granted
        if (permissionsToRequest.isEmpty()) {
            callback.onResult(true, permissions, new ArrayList<>());
        } else {
            requestPermissions(permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && requestCallback != null) {
            List<String> grantedList = new ArrayList<>();
            List<String> deniedList = new ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedList.add(permissions[i]);
                } else {
                    deniedList.add(permissions[i]);
                }
            }

            boolean allGranted = deniedList.isEmpty();
            requestCallback.onResult(allGranted, grantedList, deniedList);
            requestCallback = null;
        }
    }

    /**
     * Make status bar fully transparent
     */
    protected void makeStatusBarTransparent() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
    }

    /**
     * Apply activity transition animations
     */
    public void doTransition(int type) {
        if (type == Const.BOTTOM_TO_UP) {

            overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_none);
        } else if (type == Const.UP_TO_BOTTOM) {
            overridePendingTransition(R.anim.exit_none, R.anim.enter_from_up);

        }

    }

    /**
     * Mark user as online via API
     */
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
                                Log.w(TAG, "onResponse: Status is false. User could not be marked online.");
                            }
                        } else {
                            Log.e(TAG, "onResponse: Failed to make user online. Response code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<RestResponse> call, Throwable t) {
                        Log.e(TAG, "onFailure: Error making user online", t);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception while setting user online", e);
            }
        }
    }

    /**
     * Fetch sticker pack from API and save globally
     */
    public void getStickers() {
        Call<StickerRoot> call = RetrofitBuilder.create().getStickers();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<StickerRoot> call, Response<StickerRoot> response) {
                if (response.code() == 200 && response.body() != null && response.body().isStatus()) {
                    if (response.body().getSticker() != null && !response.body().getSticker().isEmpty()) {
                        RayziUtils.setStickers(response.body().getSticker());
                    }
                }
            }

            @Override
            public void onFailure(Call<StickerRoot> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }


    protected MainApplication application() {
        return (MainApplication) getApplication();
    }

    // Helper: register/unregister Agora RTC event handlers
    protected void registerRtcEventHandler(EventHandler handler) {
        application().registerEventHandler(handler);
    }
    protected void removeRtcEventHandler(EventHandler handler) {
        application().removeEventHandler(handler);
    }

    /**
     * Callback for permission results
     */
    public interface RequestCallback {
        void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList);
    }

    /**
     * Fetch ads keys from API and save in session
     */
    public void getAdsKeys() {
        Call<AdsRoot> call = RetrofitBuilder.create().getAds();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<AdsRoot> call, Response<AdsRoot> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    AdsRoot adsRoot = response.body();
                    if (adsRoot.getAdvertisement() != null) {
                        sessionManager.saveAds(adsRoot.getAdvertisement());
                    } else {
                        Log.w(TAG, "Advertisement is null in the response");
                    }
                } else {
                    Log.w(TAG, "Unsuccessful response or null body: " + (response.body() != null ? response.body().getMessage() : "No message"));
                }
            }

            @Override
            public void onFailure(Call<AdsRoot> call, Throwable t) {
                Log.e(TAG, "Error fetching ads: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Start UCrop image cropper with predefined options
     */
    public void startCropActivity(@NonNull Uri uri) {
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), SAMPLE_CROPPED_IMAGE_NAME + System.currentTimeMillis() + ".png"))).useSourceImageAspectRatio();
        UCrop.Options options = new UCrop.Options();
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.pink));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.pink));
        options.setToolbarColor(ContextCompat.getColor(this, R.color.lightBlack));
        options.setCropFrameColor(ContextCompat.getColor(this, R.color.colorBlack));
        options.setDimmedLayerColor(ContextCompat.getColor(this, R.color.colorBlack));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    /**
     * Convert URI → real file path
     */
    public @Nullable String getRealPathFromURI(Uri contentURI) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) {
                // Might be a file:// or a virtual doc; fall back to path (often null on 10+)
                return contentURI.getPath();
            }
            if (!cursor.moveToFirst()) return null;

            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            if (idx >= 0) {
                result = cursor.getString(idx);
            } else {
                // DATA column missing on Android 10+, return null to force cache copy path
                result = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }


    public void onClickBack(View view) {
        onBackPressed();
    }

    /**
     * Check if FloatingButtonService is currently running
     */
    public boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (FloatingButtonService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void applyRTLSupport() {
        String selectedLanguage = sessionManager.getStringValue(Const.SELECTED_LANGUAGE); // Get the selected language from SessionManager
        if (selectedLanguage == null || selectedLanguage.isEmpty()) {
            selectedLanguage = Locale.getDefault().getLanguage();
        }

        Locale locale = new Locale(selectedLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        // Check if the selected language is RTL and set layout direction accordingly
        if (isRTL(this)) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public static boolean isRTL(Context context) {

        Configuration config = context.getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            return true;
        } else {
            return false;
        }

    }

    protected void autoHideWhenKeyboardShown(View... views) {
        View root = findViewById(android.R.id.content);
        watchKeyboardOn(root, views);
    }

    private void watchKeyboardOn(@NonNull View root, View... views) {
        autoHide.clear();
        Collections.addAll(autoHide, views);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            applyVisibility(insets.isVisible(WindowInsetsCompat.Type.ime()));
            return insets;
        });

        ViewCompat.setWindowInsetsAnimationCallback(root,
                new WindowInsetsAnimationCompat.Callback(
                        WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets,
                                                         @NonNull List<WindowInsetsAnimationCompat> running) {
                        applyVisibility(insets.isVisible(WindowInsetsCompat.Type.ime()));
                        return insets;
                    }
                });
    }

    private void applyVisibility(boolean keyboardVisible) {
        for (View v : autoHide) if (v != null)
            v.setVisibility(keyboardVisible ? View.GONE : View.VISIBLE);
    }

    public void applyGradientToTextView(TextView textView, boolean isActive) {
        if (isActive) {
            Paint paint = textView.getPaint();
            float width = paint.measureText(textView.getText().toString());

            Shader textShader = new LinearGradient(
                    0f, 0f, width, textView.getTextSize(),
                    new int[]{
                            ContextCompat.getColor(this, R.color.app_gradient1),
                            ContextCompat.getColor(this, R.color.app_gradient2)
                    },
                    null,
                    Shader.TileMode.CLAMP
            );
            textView.getPaint().setShader(textShader);
        } else {
            textView.getPaint().setShader(null);
            textView.setTextColor(ContextCompat.getColor(this, R.color.gray)); // inactive color
        }
        textView.invalidate();
    }


//    public String t(int resId) {
//        try {
//            String key = getResources().getResourceEntryName(resId);
//            String translated = TranslationManager.getInstance().get(key);
//            if (translated != null && !translated.isEmpty()) {
//                return translated;
//            }
//        } catch (Exception ignored) {}
//        return getString(resId); // fallback to strings.xml
//    }

//    @Override
//    public Resources getResources() {
//        // Resources wrap kariye — getString() automatically translated value aapse
//        return new TranslationResources(super.getResources());
//    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new TranslationContextWrapper(newBase));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up socket listener
        MySocketManager.getInstance().removeCallHandler(callHandler);
    }
}
