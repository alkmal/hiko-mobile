package com.codder.ultimate.chat.viewmodel;

import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.chat.adapter.ChatAdapter;
import com.codder.ultimate.chat.modelclass.ChatItem;
import com.codder.ultimate.chat.modelclass.ChatListRoot;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends ViewModel {
    private static final String TAG = "ChatViewModel";
    public ChatAdapter chatAdapter;
    public MutableLiveData<Boolean> sendBtnEnable = new MutableLiveData<>(false);
    public MutableLiveData<String> lastMessageId = new MutableLiveData<>();

    public String chatTopic;
    public int start = 0;
    public ObservableBoolean isLoading = new ObservableBoolean(false);
    public MutableLiveData<Boolean> isLoadingComplete = new MutableLiveData<>();

    public void deleteChat(ChatItem chatDummy, int position) {
        Call<RestResponse> call = RetrofitBuilder.create().deleteChat(chatDummy.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RestResponse result = response.body();

                    if (result.isStatus()) {
                        chatAdapter.removeSingleItem(position);
                        Log.d(TAG, "deleteChat: Chat deleted successfully at position " + position);
                    } else {
                        Log.w(TAG, "deleteChat: Deletion failed - " + (result.getMessage() != null ? result.getMessage() : "Unknown reason"));
                    }
                }else {
                    Log.e(TAG, "deleteChat: Response error - Code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                Log.e(TAG, "deleteChat: Network error - " + t.getLocalizedMessage(), t);
            }
        });
    }

    public void getOldChat(boolean isLoadMore,String userId) {
        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            start = 0;
            chatAdapter.clear();
            isLoading.set(true);
        }

        Call<ChatListRoot> call = RetrofitBuilder.create().getOldChats(chatTopic,userId, start, Const.LIMIT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ChatListRoot> call, Response<ChatListRoot> response) {
                isLoading.set(false);
                isLoadingComplete.postValue(true);

                if (response.isSuccessful() && response.body() != null) {
                    ChatListRoot result = response.body();

                    if (result.isStatus() && result.getChat() != null && !result.getChat().isEmpty()) {
                        chatAdapter.addData(result.getChat());

                        ChatItem lastMessage = result.getChat().get(0);
                        if (lastMessage != null && lastMessage.getId() != null) {
                            lastMessageId.postValue(lastMessage.getId());
                        }
                        Log.d(TAG, "Loaded " + result.getChat().size() + " old chat messages.");
                    } else {
                        Log.i(TAG, "No more chat data to load.");
                    }
                } else {
                    Log.e(TAG, "getOldChat: Response failed - Code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ChatListRoot> call, Throwable t) {
                isLoading.set(false);
                isLoadingComplete.postValue(true);
                Log.e(TAG, "getOldChat: Network failure - " + t.getLocalizedMessage(), t);
            }
        });
    }
}