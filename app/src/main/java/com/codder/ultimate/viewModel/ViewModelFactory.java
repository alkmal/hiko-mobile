package com.codder.ultimate.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final ViewModel viewModel;

    public ViewModelFactory(@NonNull ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(viewModel.getClass())) {
            try {
                return (T) viewModel;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("ViewModel cast error: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Unexpected ViewModel class: " + modelClass.getName());
        }
    }

    public <T extends ViewModel> ViewModelProvider.Factory createFor() {
        return new ViewModelProvider.Factory() {
            @Override
            public <T extends ViewModel> T create(Class<T> modelClass) {
                if (modelClass.isAssignableFrom(viewModel.getClass())) {
                    return (T) viewModel;
                }
                throw new IllegalArgumentException("unexpected model class " + modelClass);
            }

        };

    }
}
