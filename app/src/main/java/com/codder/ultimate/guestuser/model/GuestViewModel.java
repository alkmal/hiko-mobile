package com.codder.ultimate.guestuser.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.guestuser.utils.UserRepository;
import com.codder.ultimate.modelclass.GuestProfileRoot;

import java.util.Objects;

public class GuestViewModel extends ViewModel {

    private final UserRepository repository;

    private final MutableLiveData<GuestProfileRoot.User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> followLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> blockLoadingLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getFollowLoading() {
        return followLoadingLiveData;
    }

    public LiveData<Boolean> getBlockLoading() {
        return blockLoadingLiveData;
    }

    public GuestViewModel(UserRepository repository) {
        this.repository = repository;
    }

    public LiveData<GuestProfileRoot.User> getUser() {
        return userLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void fetchGuestProfile(String userId) {
        loadingLiveData.setValue(true);
        repository.getGuestProfile(userId).observeForever(result -> {
            loadingLiveData.setValue(false);
            if (result.isSuccess()) {
                userLiveData.setValue(result.get());
            } else {
                errorLiveData.setValue(Objects.requireNonNull(result.exceptionOrNull()).getMessage());
            }
        });
    }

    public void followUnfollowUser(boolean follow, String userId) {
        followLoadingLiveData.setValue(true);
        repository.followUnfollowUser(follow, userId).observeForever(result -> {
            followLoadingLiveData.setValue(false);
            if (result.isSuccess()) {
                GuestProfileRoot.User currentUser = userLiveData.getValue();
                if (currentUser != null) {
                    currentUser.setFollow(follow);
                    userLiveData.setValue(currentUser);
                }
            } else {
                errorLiveData.setValue(Objects.requireNonNull(result.exceptionOrNull()).getMessage());
            }
        });
    }


    public void blockUnblockUser(String userId) {
        blockLoadingLiveData.setValue(true);
        repository.blockUnblock(userId).observeForever(result -> {
            blockLoadingLiveData.setValue(false);
            if (result.isSuccess()) {
                GuestProfileRoot.User currentUser = userLiveData.getValue();
                if (currentUser != null) {
                    currentUser.setBlock(!currentUser.isBlock());
                    userLiveData.setValue(currentUser);
                }
            } else {
                errorLiveData.setValue(Objects.requireNonNull(result.exceptionOrNull()).getMessage());
            }
        });
    }

}

