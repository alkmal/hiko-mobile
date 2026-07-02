package com.codder.ultimate.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeRefreshViewModel extends ViewModel {
    private final MutableLiveData<Long> refreshTrigger = new MutableLiveData<>();

    public LiveData<Long> getRefreshTrigger() {
        return refreshTrigger;
    }

    public void triggerRefresh() {
        refreshTrigger.setValue(System.currentTimeMillis());
    }
}
