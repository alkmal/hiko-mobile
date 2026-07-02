package com.codder.ultimate.guestuser.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.retrofit.UserApiCall;

public class UserRepository {

    private final UserApiCall userApiCall;

    public UserRepository(UserApiCall userApiCall) {
        this.userApiCall = userApiCall;
    }

    public LiveData<Result<GuestProfileRoot.User>> getGuestProfile(String userId) {
        MutableLiveData<Result<GuestProfileRoot.User>> result = new MutableLiveData<>();

        userApiCall.getGuestProfile(userId, new UserApiCall.OnGuestUserApiListener() {
            @Override
            public void onUserGot(GuestProfileRoot.User user) {
                result.postValue(Result.success(user));
            }

            @Override
            public void onFailure() {
                result.postValue(Result.failure(new Exception("Failed to fetch user profile")));
            }
        });

        return result;
    }

    public LiveData<Result<Void>> followUnfollowUser(boolean follow, String userId) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();

        userApiCall.followUnfollowUser(follow, userId, "", new UserApiCall.OnFollowUnfollowListener() {
            @Override
            public void onFollowSuccess() {
                result.postValue(Result.success(null));
            }

            @Override
            public void onUnfollowSuccess() {
                result.postValue(Result.success(null));
            }

            @Override
            public void onFail() {
                result.postValue(Result.failure(new Exception("Follow/Unfollow action failed")));
            }
        });

        return result;
    }

    public LiveData<Result<Void>> blockUnblock(String userId) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();

        userApiCall.blockUnblock(userId, new UserApiCall.OnBlockUnblockListener() {
            @Override
            public void onBlockSuccess() {
                result.postValue(Result.success(null));
            }

            @Override
            public void onUnblockSuccess() {
                result.postValue(Result.success(null));
            }
        });

        return result;
    }
}

