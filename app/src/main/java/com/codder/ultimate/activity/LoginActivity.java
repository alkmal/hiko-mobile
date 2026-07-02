package com.codder.ultimate.activity;

import static com.codder.ultimate.GoogleLoginManager.RC_SIGN_IN;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.GoogleLoginManager;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.ActivityLoginBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.live.model.LiveStreamRoot;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    ActivityLoginBinding binding;
    private static final String TAG = "LoginActivity";
    private CustomDialogClass customDialogClass;
    private String androidId, fcmToken = "";
    private boolean isActivityDestroyed = false;
    private GoogleLoginManager googleLoginManager;

    private int pendingOps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        applyGradientToTextView(binding.tvAppname,true);
        binding.tvTerms.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.tvPrivacy.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        initMain();
        initListener();
    }

    private void initListener() {
        binding.btnQuickLogin.setOnClickListener(view -> {
            beginBlockingLoading(); // show loader once at the start
            resetPending();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", "");
            jsonObject.addProperty("gender", "");
            jsonObject.addProperty("image", "");
            jsonObject.addProperty("email", androidId);
            jsonObject.addProperty("loginType", 2);

            ensureFcmTokenThen(() -> sendData(jsonObject));
        });

        binding.btnGoogleLogin.setOnClickListener(v -> {
            beginBlockingLoading();
            resetPending();
            ensureFcmTokenThen(() -> googleLoginManager.onLogin());
        });
    }

    @SuppressLint("HardwareIds")
    private void initMain() {
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        customDialogClass = new CustomDialogClass(this, R.style.customStyle);
        customDialogClass.setCancelable(false);
        initFCMToken();
        initGoogleLogin();
    }

    private void initFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fcmToken = task.getResult();
            } else {
                Log.w(TAG, "FCM token fetch failed", task.getException());
            }
        });
    }

    private void initGoogleLogin() {

        googleLoginManager = new GoogleLoginManager(this, new GoogleLoginManager.OnGoogleLoginListener() {
            @Override
            public void onLoginSuccess(GoogleLoginManager.GoogleUser googleUser) {
                JsonObject json = new JsonObject();
                json.addProperty("name", safe(googleUser.getName()));
                if (googleUser.getImage() != null && !googleUser.getImage().isEmpty()) {
                    json.addProperty("image", googleUser.getImage());
                }
                json.addProperty("email", safe(googleUser.getEmail(), androidId));
                json.addProperty("loginType", 0);
                // loader already shown before onLogin(); proceed directly
                sendData(json);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Google login failed: " + error);
                Toast.makeText(LoginActivity.this, getString(R.string.unexpected_error_occurred_please_try_again), Toast.LENGTH_SHORT).show();
                dismissDialog();
            }
        });

    }

    // === NEW: gate to ensure we tried to get a fresh FCM token before hitting backend ===
    private void ensureFcmTokenThen(Runnable onReady) {
        if (fcmToken != null && !fcmToken.isEmpty()) {
            onReady.run();
            return;
        }
        // count this as an async op tied to the loader
        pendingOps++;
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                fcmToken = task.getResult();
            } else {
                Log.w(TAG, "FCM token still unavailable; continuing", task.getException());
            }
            try {
                onReady.run();
            } finally {
                endOneOpAndMaybeDismiss();
            }
        });
    }

    private void sendData(JsonObject jsonObject) {
        // track this network call
        pendingOps++;

        String countryFlag = "https://flagcdn.com/w160/" + sessionManager.getStringValue(Const.COUNTRY_CODE).toLowerCase() + ".png";

        Log.d(TAG, "sendData: =======countryFlagImage : " + countryFlag);
        jsonObject.addProperty("age", 18);
        jsonObject.addProperty("country", sessionManager.getStringValue(Const.COUNTRY));
        jsonObject.addProperty("countryFlagImage", countryFlag);
        jsonObject.addProperty("ip", sessionManager.getStringValue(Const.IPADDRESS));
        jsonObject.addProperty("identity", androidId);
        jsonObject.addProperty("fcmToken", fcmToken != null ? fcmToken : "");

        RetrofitBuilder.create().createUser(jsonObject).enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserRoot userRoot = response.body();

                    if (userRoot.isStatus() && userRoot.getUser() != null) {
                        sessionManager.saveUser(userRoot.getUser());
                        // proceed but keep loader until live-status check finishes / we navigate
                        proceedToNextStepWithBlockingLoader();
                    } else {
                        Log.w(TAG, "API responded with failure status or missing user object.");
                        Toast.makeText(LoginActivity.this,
                                userRoot.getMessage() != null ? userRoot.getMessage() : getString(R.string.something_went_wrong_text),
                                Toast.LENGTH_SHORT).show();
                        endOneOpAndMaybeDismiss();
                    }
                } else {
                    Log.e(TAG, "Response error: Code=" + response.code() + ", Message=" + response.message());
                    Toast.makeText(LoginActivity.this, getString(R.string.unexpected_error_occurred_please_try_again), Toast.LENGTH_SHORT).show();
                    endOneOpAndMaybeDismiss();
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getLocalizedMessage(), t);
                endOneOpAndMaybeDismiss();
            }
        });
    }

    // === NEW: perform profile gate, then live status check, then navigate; loader stays up ===
    private void proceedToNextStepWithBlockingLoader() {
        UserRoot.User user = sessionManager.getUser();
        if (user == null || isEmpty(user.getUsername()) || isEmpty(user.getGender())) {
            // stop loader before navigating
            endOneOpAndMaybeDismiss();
            startActivity(new Intent(this, EditProfileActivity.class));
            return;
        }

        sessionManager.saveBooleanValue(Const.IS_LOGIN, true);

        // check live status (keep loader up)
        pendingOps++;
        RetrofitBuilder.create().checkUserLiveOrNot(sessionManager.getUser().getId())
                .enqueue(new Callback<LiveStreamRoot>() {
                    @Override
                    public void onResponse(Call<LiveStreamRoot> call, Response<LiveStreamRoot> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            LiveStreamRoot liveStreamRoot = response.body();
                            if (liveStreamRoot.isStatus()) {
                                sessionManager.setIsAudioRoomBackground(true);
                                sessionManager.saveLiveUserForBackground(new Gson().fromJson(
                                        new Gson().toJson(liveStreamRoot.getLiveUser()),
                                        PkAudioLiveUserRoot.UsersItem.class
                                ));
                            } else {
                                sessionManager.setIsAudioRoomBackground(false);
                            }
                        }

                        // ✅ close loader and then navigate
                        endOneOpAndMaybeDismiss();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }

                    @Override
                    public void onFailure(Call<LiveStreamRoot> call, Throwable t) {
                        Log.e(TAG, "Error checking live status", t);

                        // ✅ close loader and then navigate
                        endOneOpAndMaybeDismiss();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK && data != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            googleLoginManager.handleSignInResult(task);
        } else {
            Log.w(TAG, "Google sign-in canceled or failed");
            dismissDialog();
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String safe(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        dismissDialog();
    }

    @Override
    protected void onDestroy() {
        isActivityDestroyed = true;
        super.onDestroy();
    }

    private void dismissDialog() {
        if (!isActivityDestroyed && customDialogClass != null && customDialogClass.isShowing()) {
            customDialogClass.dismiss();
        }
        setButtonsEnabled(true);
    }

    // === NEW: show loader safely and disable taps to prevent double-requests ===
    private void beginBlockingLoading() {
        if (!isActivityDestroyed && customDialogClass != null && !customDialogClass.isShowing()) {
            customDialogClass.show();
        }
        setButtonsEnabled(false);
    }

    // === NEW: track async ops and close loader only when everything finished ===
    private void endOneOpAndMaybeDismiss() {
        pendingOps = Math.max(0, pendingOps - 1);
        if (pendingOps == 0) {
            dismissDialog();
        }
    }

    private void resetPending() {
        pendingOps = 0;
        setButtonsEnabled(false);
    }

    private void setButtonsEnabled(boolean enabled) {
        if (binding == null) return;
        binding.btnQuickLogin.setEnabled(enabled);
        binding.btnGoogleLogin.setEnabled(enabled);
        // optional: dim buttons while disabled
        float alpha = enabled ? 1f : 0.6f;
        binding.btnQuickLogin.setAlpha(alpha);
        binding.btnGoogleLogin.setAlpha(alpha);
        // if you have other clickable views, disable them here as well
    }
}