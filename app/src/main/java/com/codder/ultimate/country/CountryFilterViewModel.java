package com.codder.ultimate.country;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CountryFilterViewModel extends ViewModel {

    private final MutableLiveData<String> selectedCountryCode = new MutableLiveData<>("");

    public LiveData<String> getSelectedCountryCode() {
        return selectedCountryCode;
    }

    public void setCountryCode(String code) {
        selectedCountryCode.setValue(code);
    }
}

