package com.codder.ultimate.chat.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;


import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.agora.rtc.EventHandler;

import io.agora.rtc2.RtcEngine;


public abstract class VideoCallBaseActivity extends BaseActivity implements EventHandler {

    @Override
    protected void onStart() {
        super.onStart();
        BaseActivity.STATUS_VIDEO_CALL = true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.STATUS_VIDEO_CALL = true;
        registerRtcEventHandler(this);
    }

    protected RtcEngine rtcEngine() {
        return application().rtcEngine();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeRtcEventHandler(this);

    }
}
