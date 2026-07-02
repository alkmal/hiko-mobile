package com.codder.ultimate.live.model;

import com.codder.ultimate.modelclass.UserRoot;

public class LiveStramComment {
    String liveStreamingId = "";
    UserRoot.User user;
    String hostId = "";
    public String reaction;
    String type = "comment";
    String giftCount;

    public LiveStramComment(String comment, UserRoot.User userDummy, boolean isJoined, String liveStreamingId, String reaction, String type, String giftCount) {
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

    public LiveStramComment() {
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

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
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
