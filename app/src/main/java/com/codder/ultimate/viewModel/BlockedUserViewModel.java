package com.codder.ultimate.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.modelclass.BlockedUserListRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockedUserViewModel extends ViewModel {

    private final MutableLiveData<List<BlockedUserListRoot.BlockedUsersItem>> blockedUsers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<BlockedUserListRoot.BlockedUsersItem>> getBlockedUsers() {
        return blockedUsers;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchBlockedUsers(String userId) {
        isLoading.setValue(true);

        RetrofitBuilder.create().getBlockUser(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BlockedUserListRoot> call, Response<BlockedUserListRoot> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<BlockedUserListRoot.BlockedUsersItem> users = response.body().getBlockedUsers();
                    blockedUsers.setValue(users);
                    Log.d("ViewModel", "Fetched " + (users != null ? users.size() : 0) + " blocked users.");
                } else {
//                    String errorMsg = getString(R.string.failed_to_fetch_data) + (response.message() != null ? response.message() : getString(R.string.unknown_error));
//                    error.setValue(errorMsg);
                    Log.w("ViewModel", "API error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BlockedUserListRoot> call, Throwable t) {
                error.setValue("Network error: " + t.getLocalizedMessage());
                isLoading.setValue(false);
                Log.e("ViewModel", "Network failure while fetching blocked users", t);
            }
        });
    }
}