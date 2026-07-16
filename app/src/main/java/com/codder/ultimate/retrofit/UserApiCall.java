package com.codder.ultimate.retrofit;

import android.content.Context;
import android.util.Log;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.chat.modelclass.ChatSuggestion;
import com.codder.ultimate.chat.modelclass.ChatTopicRoot;
import com.codder.ultimate.guestuser.model.FollowUnfollowResponse;
import com.codder.ultimate.modelclass.BlockUnblockRoot;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserApiCall {

    private static final String TAG = "UserApiCall";
    SessionManager sessionManager;
    private Context context;

    public UserApiCall(Context context) {
        this.context = context;
        sessionManager = new SessionManager(context);
    }


    public void getUser(OnUserApiListener onUserApiListener) {
        String userId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : null;

        Call<UserRoot> call = RetrofitBuilder.create().getUser(userId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserRoot userRoot = response.body();

                    if (userRoot.isStatus()) {
                        if (userRoot.getUser() != null) {
                            onUserApiListener.onUserGot(userRoot.getUser());
                        } else {
                            Log.w("TAG", "User object is null despite successful status.");
                            onUserApiListener.onUserStatusFailed("User data missing in response.");
                        }
                    } else {
                        Log.w("TAG", "API returned failure: " + userRoot.getMessage());
                        onUserApiListener.onUserStatusFailed(userRoot.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.d("TAG", "onFailure: " + t.getMessage());
            }
        });
    }


    public void blockUnblock(String guestId, OnBlockUnblockListener onBlockUnblockListener) {
        String userId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : null;

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("toUserId", guestId);

        Call<BlockUnblockRoot> call = RetrofitBuilder.create().BlockUser(sessionManager.getUser().getId(), guestId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BlockUnblockRoot> call, Response<BlockUnblockRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BlockUnblockRoot result = response.body();

                    if (result.isStatus()) {
                        if (result.isIsBlocked()) {
                            onBlockUnblockListener.onBlockSuccess();
                        } else {
                            onBlockUnblockListener.onUnblockSuccess();
                        }
                    } else {
                        String message = result.getMessage() != null ? result.getMessage() : "Unknown server response.";
                        Log.w("BlockUnblock", "API responded with failure: " + message);
                    }
                } else {
                    String errorMessage = "Failed with HTTP code: " + response.code();
                    Log.e("BlockUnblock", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<BlockUnblockRoot> call, Throwable t) {
                Log.e("BlockUnblock", "Network failure: " + t.getLocalizedMessage(), t);
            }
        });
    }


    public void createChatTopic(String localId, String guestId, OnChatTopicCreateLister onChatTopicCreateLister) {
        if (localId == null || guestId == null || localId.isEmpty() || guestId.isEmpty()) {
            Log.e(TAG, "createChatTopic: Invalid user IDs.");
            onChatTopicCreateLister.onTopicCreated("");
            return;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("senderUserId", localId);
        jsonObject.addProperty("receiverUserId", guestId);
        Call<ChatTopicRoot> call = RetrofitBuilder.create().createChatRoom(jsonObject);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ChatTopicRoot> call, Response<ChatTopicRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatTopicRoot root = response.body();

                    if (root.isStatus()) {
                        String topicId = root.getChatTopic() != null ? root.getChatTopic().getId() : "";
                        if (topicId != null && !topicId.isEmpty()) {
                            Log.d(TAG, "Chat topic created successfully: " + topicId);
                            onChatTopicCreateLister.onTopicCreated(topicId);
                            return;
                        }
                    }

                    Log.w(TAG, "Chat topic creation failed or empty ID.");
                    onChatTopicCreateLister.onTopicCreated("");
                } else {
                    Log.e(TAG, "createChatTopic: Response error - Code: " + response.code() + ", Message: " + response.message());
                    onChatTopicCreateLister.onTopicCreated("");
                }
            }

            @Override
            public void onFailure(Call<ChatTopicRoot> call, Throwable t) {
                Log.e(TAG, "createChatTopic: Network failure - " + t.getLocalizedMessage(), t);
                onChatTopicCreateLister.onTopicCreated("");
            }
        });

    }

    public void getGuestProfile(String guestId, OnGuestUserApiListener onGuestUserApiListener) {
        if (guestId == null || guestId.trim().isEmpty()) {
            Log.e(TAG, "getGuestProfile: Invalid guest ID.");
            onGuestUserApiListener.onFailure();
            return;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("fromUserId", sessionManager.getUser().getId());
        jsonObject.addProperty("toUserId", guestId);
        jsonObject.addProperty("profileUserId", guestId);
        Call<GuestProfileRoot> call = RetrofitBuilder.create().getGuestUser(jsonObject);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GuestProfileRoot> call, Response<GuestProfileRoot> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GuestProfileRoot result = response.body();

                    if (result.isStatus() && result.getUser() != null) {
                        onGuestUserApiListener.onUserGot(result.getUser());
                        Log.d(TAG, "getGuestProfile: User data fetched successfully.");
                    } else {
                        Log.w(TAG, "getGuestProfile: Failed to fetch user profile. Status: " + result.getMessage());
                        onGuestUserApiListener.onFailure();
                    }
                } else {
                    Log.e(TAG, "getGuestProfile: Response error. Code: " + response.code() + ", Message: " + response.message());
                    onGuestUserApiListener.onFailure();
                }
            }

            @Override
            public void onFailure(Call<GuestProfileRoot> call, Throwable t) {
                Log.e(TAG, "getGuestProfile: Network failure - " + t.getLocalizedMessage(), t);
                onGuestUserApiListener.onFailure();
            }
        });
    }

    public interface OnChatTopicCreateLister {
        void onTopicCreated(String s);
    }

    public interface OnUserApiListener {
        void onUserGot(UserRoot.User user);

        void onUserStatusFailed(String message);
    }

    public interface OnBlockUnblockListener {
        void onBlockSuccess();

        void onUnblockSuccess();

    }

    public interface OnFollowUnfollowListener {
        void onFollowSuccess();

        void onUnfollowSuccess();

        void onFail();
    }

    public void followUnfollowUser(boolean followNow, String guestId, String liveStreamingId, OnFollowUnfollowListener onFollowUnfollowListener) {
        if (guestId == null || guestId.isEmpty()) {
            Log.e(TAG, "followUnfollowUser: Invalid guest ID.");
            onFollowUnfollowListener.onFail();
            return;
        }
        if (sessionManager.getUser() != null && guestId.equals(sessionManager.getUser().getId())) {
            Log.w(TAG, "followUnfollowUser: Ignoring self-follow request.");
            onFollowUnfollowListener.onFail();
            return;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("fromUserId", sessionManager.getUser().getId());
        jsonObject.addProperty("toUserId", guestId);
        if (!liveStreamingId.isEmpty()) {
            jsonObject.addProperty("liveStreamingId", liveStreamingId);
        }
        Call<FollowUnfollowResponse> call = RetrofitBuilder.create().toggleFollowUnfollow(jsonObject);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<FollowUnfollowResponse> call, Response<FollowUnfollowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FollowUnfollowResponse result = response.body();

                    if (result.isStatus()) {
                        if (result.isFollow()) {
                            onFollowUnfollowListener.onFollowSuccess();
                        } else {
                            onFollowUnfollowListener.onUnfollowSuccess();
                        }
                        Log.d(TAG, "Follow/Unfollow action successful.");
                    } else {
                        Log.w(TAG, "Follow/Unfollow failed: " + result.getMessage());
                        onFollowUnfollowListener.onFail();
                    }
                } else {
                    Log.e(TAG, "Error response code: " + response.code() + ", Message: " + response.message());
                    onFollowUnfollowListener.onFail();
                }
            }

            @Override
            public void onFailure(Call<FollowUnfollowResponse> call, Throwable t) {
                Log.e(TAG, "Network error while following/unfollowing: " + t.getMessage(), t);
                onFollowUnfollowListener.onFail();
            }
        });
    }

    public interface OnGuestUserApiListener {
        void onUserGot(GuestProfileRoot.User user);

        void onFailure();
    }

    public void getChatSuggestions(OnChatSuggestionsListener listener) {
        RetrofitBuilder.create().getAllSuggestedMessages().enqueue(new Callback<ChatSuggestion>() {
            @Override
            public void onResponse(Call<ChatSuggestion> call, Response<ChatSuggestion> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listener.onSuggestionsFetched(response.body().getData());
                } else {
                    listener.onFailure();
                }
            }

            @Override
            public void onFailure(Call<ChatSuggestion> call, Throwable t) {
                listener.onFailure();
            }
        });
    }

    public interface OnChatSuggestionsListener {
        void onSuggestionsFetched(List<ChatSuggestion.DataItem> suggestions);

        void onFailure();
    }

}
