package com.codder.ultimate.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AppBarControlViewModel extends ViewModel {
    private final MutableLiveData<Boolean> expandEvent = new MutableLiveData<>();

    public LiveData<Boolean> expandEvents() {
        return expandEvent;
    }

    public void requestExpand() {
        expandEvent.setValue(true);
    }

    public void requestCollapse() {
        expandEvent.setValue(false);
    }
}
