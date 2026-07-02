package com.codder.ultimate.live.model;

import com.codder.ultimate.modelclass.UserRoot;

public class PKLiveStreamComment {
    String liveStreamingId = "";
    UserRoot.User user;



    public String reaction;
    String type = "comment";
    String giftCount;
    boolean isPkRunning;
    String liveHostId = "";

    public PKLiveStreamComment(String comment, UserRoot.User userDummy, boolean isJoined, String liveStreamingId, String liveHostId, boolean isPkRunning) {
        this.comment = comment;
        this.liveStreamingId = liveStreamingId;
        this.user = userDummy;
        this.isJoined = isJoined;
        this.liveHostId = liveHostId;
        this.isPkRunning = isPkRunning;
    }

    public PKLiveStreamComment(String comment, UserRoot.User userDummy, boolean isJoined, String liveStreamingId, String reaction, String type, String giftCount) {
        this.comment = comment;
        this.liveStreamingId = liveStreamingId;
        this.user = userDummy;
        this.isJoined = isJoined;
        this.reaction = reaction;
        this.type = type;
        this.giftCount = giftCount;
    }

    String comment;

    public String getLiveStreamingId() {
        return liveStreamingId;
    }

    boolean isJoined = false;

    public PKLiveStreamComment() {
    }

    @Override
    public String toString() {
        return "LiveStramComment{" +
                "liveStreamingId='" + liveStreamingId + '\'' +
                ", user=" + user +
                ", comment='" + comment + '\'' +
                ", isJoined=" + isJoined +
                '}';
    }

    public String getLiveHostId() {
        return liveHostId;
    }

    public void setLiveHostId(String liveHostId) {
        this.liveHostId = liveHostId;
    }

    public boolean isPkRunning() {
        return isPkRunning;
    }

    public void setPkRunning(boolean pkRunning) {
        isPkRunning = pkRunning;
    }
    public void setLiveStreamingId(String liveStreamingId) {
        this.liveStreamingId = liveStreamingId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public UserRoot.User getUser() {
        return user;
    }

    public void setUser(UserRoot.User userDummy) {
        this.user = userDummy;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }

    public String getReaction() {
        return reaction;
    }

    public String getType() {
        return type;
    }

    public String getGiftCount() {
        return giftCount;
    }
}
