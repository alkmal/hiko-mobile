package com.codder.ultimate.retrofit;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.modelclass.RestResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentApiCalling {

    private final SessionManager sessionManager;
    private static final String TAG = "CommentApiCalling";

    public CommentApiCalling(@NonNull Context context) {
        this.sessionManager = new SessionManager(context.getApplicationContext());
    }

    public void toggleLikePost(@NonNull String postId, @NonNull OnToggleLikeListener onToggleLikeListener) {
        String userId = getUserId();
        if (userId == null || postId.isEmpty()) {
            Log.w(TAG, "toggleLikePost: Invalid input");
            return;
        }

        RetrofitBuilder.create().toggleLikePost(userId, postId).enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(@NonNull Call<RestResponse> call, @NonNull Response<RestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean isLiked = response.body().isLiked();
                    onToggleLikeListener.onToggleLiked(isLiked);
                    Log.d(TAG, "toggleLikePost: Success, isLiked = " + isLiked);
                } else {
                    Log.w(TAG, "toggleLikePost: Unsuccessful response. Code = " + response.code() +
                            ", Message = " + response.message());
                    onToggleLikeListener.onToggleLiked(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestResponse> call, @NonNull Throwable t) {
                onToggleLikeListener.onToggleLiked(null);
                Log.e(TAG, "toggleLikePost failed: " + t.getMessage(), t);
            }
        });
    }

    public void toggleLikeRelite(@NonNull String reliteId, @NonNull OnToggleLikeListener onToggleLikeListener) {
        String userId = getUserId();
        if (userId == null || reliteId.isEmpty()) {
            Log.w(TAG, "toggleLikeRelite: Invalid input");
            return;
        }

        RetrofitBuilder.create().toggleLikeReel(userId, reliteId).enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(@NonNull Call<RestResponse> call, @NonNull Response<RestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean isLiked = response.body().isLiked();
                    onToggleLikeListener.onToggleLiked(isLiked);
                    Log.d(TAG, "toggleLikeReel: Success, isLiked = " + isLiked);
                } else {
                    Log.w(TAG, "toggleLikeReel: Response failed — HTTP " + response.code() +
                            ", Message: " + response.message());
                    onToggleLikeListener.onToggleLiked(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestResponse> call, @NonNull Throwable t) {
                onToggleLikeListener.onToggleLiked(null);
                Log.e(TAG, "toggleLikeRelite failed: " + t.getMessage(), t);
            }
        });
    }

    private String getUserId() {
        return sessionManager.getUser() != null ? sessionManager.getUser().getId() : null;
    }

    public interface OnToggleLikeListener {
        void onToggleLiked(@Nullable Boolean isLiked);
    }

}
