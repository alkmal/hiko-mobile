package com.codder.ultimate.launguagetranslation;

import android.content.Context;
import android.content.res.Resources;
import android.content.ContextWrapper;

import androidx.annotation.NonNull;

public class TranslationContextWrapper extends ContextWrapper {

    public TranslationContextWrapper(Context base) {
        super(base);
    }

    @Override
    public Resources getResources() {
        return new TranslationResources(super.getResources());
    }

}