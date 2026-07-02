package com.codder.ultimate.provider;

import android.content.Context;

import com.vaibhavpandey.katora.contracts.MutableContainer;
import com.vaibhavpandey.katora.contracts.Provider;

public class ExoPlayerProvider implements Provider {

    private final Context mContext;

    public ExoPlayerProvider(Context context) {
        mContext = context;
    }

    @Override
    public void provide(MutableContainer container) {

    }
}
