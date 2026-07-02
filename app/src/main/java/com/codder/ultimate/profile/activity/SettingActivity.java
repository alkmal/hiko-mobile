package com.codder.ultimate.profile.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.activity.ComplainListActivity;
import com.codder.ultimate.activity.CreateComplainActivity;
import com.codder.ultimate.activity.SplashActivity;
import com.codder.ultimate.databinding.ActivitySettingBinding;
import com.codder.ultimate.language.SelectLanguageActivity;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.socket.MySocketManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class SettingActivity extends BaseActivity {

    private ActivitySettingBinding binding;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    binding.switchNotification.setChecked(true);
                    sessionManager.notificationOnOff(true);
                    Toast.makeText(this, getString(R.string.notification_permission_granted), Toast.LENGTH_SHORT).show();
                } else {
                    // Permission denied. Decide whether to show rationale or go to settings
                    binding.switchNotification.setChecked(false);
                    sessionManager.notificationOnOff(false);

                    new PopupBuilder(SettingActivity.this)
                            .showPopUpWithVector(R.drawable.vector_notification,
                                    getString(R.string.notification_permission_denied),
                                    getString(R.string.you_have_to_enable_it_from_settings),
                                    getString(R.string.open_settings), this::openAppNotificationSettings);
                }
            });

    private CompoundButton.OnCheckedChangeListener notificationSwitchListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        binding.tvVersion.setText(getString(R.string.version) + BuildConfig.VERSION_CODE);

        initView();
        initListeners();

        notificationSwitchListener = (buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        sessionManager.notificationOnOff(true);
                    } else {
                        binding.switchNotification.setChecked(false);
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                } else {
                    if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                        sessionManager.notificationOnOff(true);
                    } else {
                        binding.switchNotification.setChecked(false);
                        Toast.makeText(this, "You have to enable it from settings.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                sessionManager.notificationOnOff(false);
            }
        };

        binding.switchNotification.setOnCheckedChangeListener(notificationSwitchListener);

    }

    private void initView() {

        if (sessionManager != null) {
            binding.switchNotification.setChecked(sessionManager.isNotificationOn());
        }

        binding.lytSupport.setOnClickListener(v ->
                startActivity(new Intent(this, CreateComplainActivity.class))
        );

        binding.lytComplains.setOnClickListener(v ->
                startActivity(new Intent(this, ComplainListActivity.class))
        );

        if (isRTL(this)) {
            binding.notification.setGravity(Gravity.END);
            binding.termsOfService.setGravity(Gravity.END);
            binding.privacyPolicy.setGravity(Gravity.END);
            binding.aboutUs.setGravity(Gravity.END);
            binding.logout.setGravity(Gravity.END);
        }
    }

    private void initListeners() {


        binding.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        sessionManager.notificationOnOff(true);
                    } else {
                        // Immediately revert the UI; request permission
                        binding.switchNotification.setChecked(false);
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                } else {
                    // Android 12 and below: user may have blocked notifications at OS level
                    if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                        sessionManager.notificationOnOff(true);
                    } else {
                        binding.switchNotification.setChecked(false);
                        Toast.makeText(this,
                                "Notifications are disabled. Please enable them in settings.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                sessionManager.notificationOnOff(false);
            }
        });


        binding.btnLogout.setOnClickListener(v -> showLogoutPopup());


        binding.lytLanguage.setOnClickListener(v ->
                startActivity(new Intent(SettingActivity.this, SelectLanguageActivity.class)));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.notification_permission_granted, Toast.LENGTH_SHORT).show();
                binding.switchNotification.setChecked(true); // Now turn ON
                sessionManager.notificationOnOff(true);
            } else {
                Toast.makeText(this, getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show();
                binding.switchNotification.setChecked(false); // Keep OFF
            }
        }
    }

    private void openAppNotificationSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void showLogoutPopup() {
        new PopupBuilder(this).showLogoutPopup(
                R.drawable.vector_logout,
                getString(R.string.log_out),
                getString(R.string.do_you_really_want_to_log_out),
                getString(R.string.continue_text),
                getString(R.string.cancel),
                this::logoutUser
        );
    }

    private void logoutUser() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleSignInClient.signOut();

            if (sessionManager != null) {
                sessionManager.saveBooleanValue(Const.IS_LOGIN, false);
                sessionManager.ClearAllData();
            }

            if (MySocketManager.getInstance().getSocket() != null) {
                MySocketManager.getInstance().getSocket().disconnect();
            }

            Intent intent = new Intent(this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.failed_to_logout_please_try_again), Toast.LENGTH_SHORT).show();
        }
    }


    public void onClickPrivacy(View view) {
        openWebPage(getString(R.string.privacy_policy), sessionManager != null ? sessionManager.getSetting().getPrivacyPolicyLink() : "");
    }

    public void onClickAbout(View view) {
        openWebPage(getString(R.string.about_us), sessionManager != null ? sessionManager.getSetting().getAboutUsLink() : "");
    }

    public void onClickTerms(View view) {
        openWebPage(getString(R.string.terms_of_service), sessionManager != null ? sessionManager.getSetting().getTermsAndConditionLink() : "");
    }

    private void openWebPage(String title, String url) {
        if (url != null && !url.isEmpty()) {
            WebActivity.open(this, title, url, true);
        } else {
            Toast.makeText(this, getString(R.string.link_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean enabled;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            enabled = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    && NotificationManagerCompat.from(this).areNotificationsEnabled();
        } else {
            enabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        }

        // Update switch without re-triggering the listener
        binding.switchNotification.setOnCheckedChangeListener(null);
        binding.switchNotification.setChecked(enabled);
        sessionManager.notificationOnOff(enabled);
        binding.switchNotification.setOnCheckedChangeListener(notificationSwitchListener);
    }
}
