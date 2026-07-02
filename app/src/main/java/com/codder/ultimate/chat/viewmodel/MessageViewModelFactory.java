package com.codder.ultimate.chat.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.SessionManager;

public class MessageViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public MessageViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MessageViewModel.class)) {
            return (T) new MessageViewModel(new SessionManager(context));
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
