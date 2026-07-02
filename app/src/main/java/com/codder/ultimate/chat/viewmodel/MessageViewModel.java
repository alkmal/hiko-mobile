package com.codder.ultimate.chat.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.chat.modelclass.ChatUserListRoot;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageViewModel extends ViewModel {

    private static final String TAG = "MessageViewModel";
    private final SessionManager sessionManager;
    public final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);


    public MessageViewModel(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private final MutableLiveData<List<ChatUserListRoot.ChatUserItem>> chatUserItems = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<ChatUserListRoot.ChatUserItem>> chatUsers = chatUserItems;

    private final MutableLiveData<Boolean> noData = new MutableLiveData<>(false);
    public final LiveData<Boolean> isNoData = noData;

    private final MutableLiveData<Boolean> loadingStatus = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = loadingStatus;

    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    public final LiveData<String> messageToast = toastMessage;

    private int start = 0;
    private boolean isFakeMode = false;

    public void fetchChatUsers(boolean isLoadMore) {
        if (!sessionManager.getBooleanValue(Const.IS_LOGIN) || sessionManager.getUser() == null) {
            toastMessage.setValue("User not logged in.");
            return;
        }

        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            start = 0;
            noData.setValue(false);

        }

        loadingStatus.setValue(true);
        RetrofitBuilder.create().getChatUserList(sessionManager.getUser().getId(), start, Const.LIMIT)
                .enqueue(new Callback<ChatUserListRoot>() {
                    @Override
                    public void onResponse(Call<ChatUserListRoot> call, Response<ChatUserListRoot> response) {
                        loadingStatus.setValue(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            List<ChatUserListRoot.ChatUserItem> newUsers = response.body().getChatList();

                            if (newUsers != null) {
                                // Separate real and fake users
                                List<ChatUserListRoot.ChatUserItem> realUsers = new ArrayList<>();
                                List<ChatUserListRoot.ChatUserItem> fakeUsers = new ArrayList<>();

                                for (ChatUserListRoot.ChatUserItem user : newUsers) {
                                    if (user.isFake()) {
                                        fakeUsers.add(user);
                                    } else {
                                        realUsers.add(user);
                                    }
                                }

                                // Determine mode on first page
                                if (!isLoadMore) {
                                    isFakeMode = realUsers.isEmpty();
                                    Log.d(TAG, "Fake mode: " + isFakeMode);
                                }

                                // Choose what to display
                                List<ChatUserListRoot.ChatUserItem> filteredUsers = isFakeMode ? fakeUsers : realUsers;

                                if (!isLoadMore) {
                                    chatUserItems.setValue(filteredUsers);
                                } else {
                                    List<ChatUserListRoot.ChatUserItem> currentItems = chatUserItems.getValue();
                                    if (currentItems == null) currentItems = new ArrayList<>();

                                    for (ChatUserListRoot.ChatUserItem newItem : filteredUsers) {
                                        boolean exists = false;
                                        for (ChatUserListRoot.ChatUserItem existingItem : currentItems) {
                                            if (existingItem.getUserId().equals(newItem.getUserId())) {
                                                exists = true;
                                                break;
                                            }
                                        }
                                        if (!exists) {
                                            currentItems.add(newItem);
                                        }
                                    }

                                    chatUserItems.setValue(currentItems);
                                }
                            }

                            noData.setValue(chatUserItems.getValue() == null || chatUserItems.getValue().isEmpty());

                            Log.d(TAG, "Loaded " + (newUsers != null ? newUsers.size() : 0) + " chat users");
                        } else {
                            String errorMsg = (response.body() != null && response.body().getMessage() != null)
                                    ? response.body().getMessage()
                                    : "Failed to fetch chat users.";
                            Log.w(TAG, "getChatUserList: " + errorMsg);
                            toastMessage.setValue(errorMsg);
                            noData.setValue(true);
                        }
                    }


                    @Override
                    public void onFailure(Call<ChatUserListRoot> call, Throwable t) {
                        loadingStatus.setValue(false);
                        toastMessage.setValue("Network error: " + t.getLocalizedMessage());
                        Log.e(TAG, "getChatUserList failed: " + t.getLocalizedMessage(), t);
                    }
                });
    }

    public void deleteAllChats() {
        if (!sessionManager.getBooleanValue(Const.IS_LOGIN) || sessionManager.getUser() == null) {
            toastMessage.setValue("User not logged in.");
            return;
        }

        RetrofitBuilder.create().deleteAllChat(sessionManager.getUser().getId()).enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RestResponse result = response.body();

                    chatUserItems.setValue(new ArrayList<>());
                    noData.setValue(true);
                    start = 0;

                    String message = result.getMessage() != null ? result.getMessage() : "All chats deleted.";
                    toastMessage.setValue(message);

                    Log.d(TAG, "Chats deleted successfully.");
                } else {
                    String errorMsg = response.body() != null && response.body().getMessage() != null
                            ? response.body().getMessage()
                            : "Failed to delete chats.";
                    toastMessage.setValue(errorMsg);
                    Log.w(TAG, "deleteAllChat: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                String error = "Error: " + t.getLocalizedMessage();
                toastMessage.setValue(error);
                Log.e(TAG, "deleteAllChat failed", t);
            }
        });
    }
}
