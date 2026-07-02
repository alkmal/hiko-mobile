package com.codder.ultimate.live.model;

import com.google.gson.annotations.SerializedName;

public class SeatItem {

    @SerializedName("image")
    private String image;

    public String getAvatarFrame() {
        return avatarFrame;
    }

    @SerializedName("avatarFrame")
    private String avatarFrame;
    @SerializedName("country")
    private String country;
    @SerializedName("reserved")
    private boolean reserved;
    @SerializedName("name")
    private String name;
    @SerializedName("lock")
    private boolean lock;
    @SerializedName("agoraUid")
    private int agoraUid;
    @SerializedName("mute")
    private int mute;
    @SerializedName("isSpeaking")
    private boolean isSpeaking;
    @SerializedName("_id")
    private String id;
    @SerializedName("position")
    private int position;
    @SerializedName("invite")
    private boolean invite;
    @SerializedName("userId")
    private String userId;

    @Override
    public String toString() {
        return "SeatItem{" +
                "image='" + image + '\'' +
                ", country='" + country + '\'' +
                ", reserved=" + reserved +
                ", name='" + name + '\'' +
                ", lock=" + lock +
                ", agoraUid=" + agoraUid +
                ", mute=" + mute +
                ", id='" + id + '\'' +
                ", position=" + position +
                ", invite=" + invite +
                ", userId='" + userId + '\'' +
                '}';
    }

    public String getImage() {
        return image;
    }

    public String getCountry() {
        return country;
    }

    public boolean isReserved() {
        return reserved;
    }

    public String getName() {
        return name;
    }

    public boolean isLock() {
        return lock;
    }

    public int getAgoraUid() {
        return agoraUid;
    }

    public int isMute() {
        return mute;
    }

    public String getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public boolean isInvite() {
        return invite;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void setSpeaking(boolean speaking) {
        isSpeaking = speaking;
    }
}
