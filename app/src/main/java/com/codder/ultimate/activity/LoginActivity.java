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
import android.widget.EditText;
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
import com.codder.ultimate.profile.activity.WebActivity;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Locale;

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
    private boolean isRegisterMode = false;

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
        updateAuthMode(false);

        binding.btnLogin.setOnClickListener(view -> submitUsernamePassword());

        binding.tvToggleMode.setOnClickListener(view -> {
            isRegisterMode = !isRegisterMode;
            updateAuthMode(true);
        });

        binding.btnGoogleLogin.setOnClickListener(v -> {
            beginBlockingLoading();
            resetPending();
            ensureFcmTokenThen(() -> googleLoginManager.onLogin());
        });
    }

    private void submitUsernamePassword() {
        String username = textOf(binding.etUsername);
        String password = textOf(binding.etPassword);
        String displayName = textOf(binding.etDisplayName);
        String confirmPassword = textOf(binding.etConfirmPassword);

        if (username.length() < 3) {
            binding.etUsername.setError("Enter at least 3 characters");
            binding.etUsername.requestFocus();
            return;
        }

        if (password.length() < 4) {
            binding.etPassword.setError("Enter at least 4 characters");
            binding.etPassword.requestFocus();
            return;
        }

        if (isRegisterMode) {
            if (displayName.isEmpty()) {
                binding.etDisplayName.setError("Enter your name");
                binding.etDisplayName.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                binding.etConfirmPassword.setError("Passwords do not match");
                binding.etConfirmPassword.requestFocus();
                return;
            }
        }

        beginBlockingLoading();
        resetPending();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("name", isRegisterMode ? displayName : username);
        jsonObject.addProperty("email", username.contains("@") ? username : username + "@vola.local");
        jsonObject.addProperty("gender", Const.MALE);
        jsonObject.addProperty("image", "");
        jsonObject.addProperty("loginType", 3);
        jsonObject.addProperty("firebaseUid", "local:" + username.toLowerCase(Locale.US));
        jsonObject.addProperty("authType", isRegisterMode ? "register" : "login");
        jsonObject.addProperty("isRegister", isRegisterMode);

        ensureFcmTokenThen(() -> sendData(jsonObject));
    }

    private void updateAuthMode(boolean clearPassword) {
        binding.tvFormTitle.setText(isRegisterMode ? "Create account" : "Login");
        binding.tvModeSubtitle.setText(isRegisterMode ? "Create a username and password" : "Use your username and password");
        binding.btnLogin.setText(isRegisterMode ? "Create account" : "Login");
        binding.tvToggleMode.setText(isRegisterMode ? "Already have an account? Login" : "Create new account");
        binding.nameWrapper.setVisibility(isRegisterMode ? View.VISIBLE : View.GONE);
        binding.confirmWrapper.setVisibility(isRegisterMode ? View.VISIBLE : View.GONE);

        if (clearPassword) {
            binding.etPassword.setText("");
            binding.etConfirmPassword.setText("");
        }
    }

    private String textOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    public void onClickPrivacy(View view) {
        if (sessionManager != null && sessionManager.getSetting() != null
                && sessionManager.getSetting().getPrivacyPolicyLink() != null
                && !sessionManager.getSetting().getPrivacyPolicyLink().isEmpty()) {
            WebActivity.open(this, getString(R.string.privacy_policy), sessionManager.getSetting().getPrivacyPolicyLink(), true);
        } else {
            Toast.makeText(this, getString(R.string.link_not_available), Toast.LENGTH_SHORT).show();
        }
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
                json.addProperty("firebaseUid", "google:" + safe(googleUser.getEmail(), androidId).toLowerCase(Locale.US));
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

        String countryCode = safe(sessionManager.getStringValue(Const.COUNTRY_CODE), "ps").toLowerCase(Locale.US);
        String country = safe(sessionManager.getStringValue(Const.COUNTRY), "Palestine");
        String countryFlag = "https://flagcdn.com/w160/" + countryCode + ".png";

        Log.d(TAG, "sendData: =======countryFlagImage : " + countryFlag);
        jsonObject.addProperty("age", 18);
        jsonObject.addProperty("country", country);
        jsonObject.addProperty("countryFlagImage", countryFlag);
        jsonObject.addProperty("ip", safe(sessionManager.getStringValue(Const.IPADDRESS), ""));
        jsonObject.addProperty("identity", androidId);
        jsonObject.addProperty("fcmToken", fcmToken != null ? fcmToken : "");
        if (!jsonObject.has("firebaseUid")) {
            String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : androidId;
            jsonObject.addProperty("firebaseUid", "local:" + safe(email, androidId).toLowerCase(Locale.US));
        }

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
                Toast.makeText(LoginActivity.this, getString(R.string.unexpected_error_occurred_please_try_again), Toast.LENGTH_SHORT).show();
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
        binding.btnLogin.setEnabled(enabled);
        binding.tvToggleMode.setEnabled(enabled);
        binding.btnGoogleLogin.setEnabled(enabled);
        // optional: dim buttons while disabled
        float alpha = enabled ? 1f : 0.6f;
        binding.btnLogin.setAlpha(alpha);
        binding.tvToggleMode.setAlpha(alpha);
        binding.btnGoogleLogin.setAlpha(alpha);
        // if you have other clickable views, disable them here as well
    }
}
