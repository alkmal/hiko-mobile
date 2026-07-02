package com.codder.ultimate.guestuser.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.guestuser.model.GuestViewModel;

public class GuestViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository repository;

    public GuestViewModelFactory(UserRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GuestViewModel.class)) {
            return (T) new GuestViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
