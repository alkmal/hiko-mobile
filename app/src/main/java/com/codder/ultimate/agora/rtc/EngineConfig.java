package com.codder.ultimate.agora.rtc;

public class EngineConfig {

    private String mChannelName;
    private boolean mShowVideoStats;
    private int mDimenIndex = 2;  //  import class issue static value written by developer 😊
    private int mMirrorLocalIndex;
    private int mMirrorRemoteIndex;
    private int mMirrorEncodeIndex;


    public int getVideoDimenIndex() {
        return mDimenIndex;
    }

    public void setVideoDimenIndex(int index) {
        mDimenIndex = index;
    }

    public String getChannelName() {
        return mChannelName;
    }

    public void setChannelName(String mChannel) {
        this.mChannelName = mChannel;
    }

    public boolean ifShowVideoStats() {
        return mShowVideoStats;
    }

    public void setIfShowVideoStats(boolean show) {
        mShowVideoStats = show;
    }

    public int getMirrorLocalIndex() {
        return mMirrorLocalIndex;
    }

    public void setMirrorLocalIndex(int index) {
        mMirrorLocalIndex = index;
    }

    public int getMirrorRemoteIndex() {
        return mMirrorRemoteIndex;
    }

    public void setMirrorRemoteIndex(int index) {
        mMirrorRemoteIndex = index;
    }

    public int getMirrorEncodeIndex() {
        return mMirrorEncodeIndex;
    }

    public void setMirrorEncodeIndex(int index) {
        mMirrorEncodeIndex = index;
    }
}
