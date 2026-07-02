package com.codder.ultimate;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class GoogleLoginManager {

    public static final int RC_SIGN_IN = 100;
    private static final String TAG = "GoogleLoginManager";

    private final Activity context;
    private final GoogleSignInClient mGoogleSignInClient;
    private final OnGoogleLoginListener listener;

    private GoogleUser googleUser = new GoogleUser();

    public GoogleLoginManager(@NonNull Activity context, @NonNull OnGoogleLoginListener listener) {
        this.context = context;
        this.listener = listener;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public void onLogin() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            // After sign-out, force account chooser
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            context.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                googleUser.setName(safe(account.getDisplayName()));
                googleUser.setEmail(safe(account.getEmail()));
                googleUser.setImage(account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "");

                listener.onLoginSuccess(googleUser);
            } else {
                listener.onFailure("GoogleSignInAccount is null");
            }

        } catch (ApiException e) {
            Log.w(TAG, "Google sign-in failed: code=" + e.getStatusCode(), e);
            listener.onFailure("Google Sign-In failed: " + e.getStatusCode());
        }
    }

    public interface OnGoogleLoginListener {
        void onLoginSuccess(GoogleUser googleUser);

        void onFailure(String error);
    }


    private String safe(String value) {
        return value != null ? value : "";
    }


    public static class GoogleUser {
        private String name;
        private String email;
        private String image;
        private String gender;
        private String bdate;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name != null ? name : "";
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email != null ? email : "";
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image != null ? image : "";
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender != null ? gender : "";
        }

        public String getBirthYear() {
            return bdate;
        }

        public void setBirthYear(String bdate) {
            this.bdate = bdate != null ? bdate : "";
        }
    }


}
