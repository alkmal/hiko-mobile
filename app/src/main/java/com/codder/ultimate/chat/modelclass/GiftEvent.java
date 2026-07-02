package com.codder.ultimate.chat.modelclass;


import androidx.annotation.NonNull;

public class GiftEvent {
    public final String senderId;
    public final String senderName;
    public final String senderAvatar;   // absolute or relative your UserProfileImageView can handle
    public final String giftUrl;        // absolute (prefer) or relative
    public final int count;
    public final long timestampMs;

    public GiftEvent(String senderId, String senderName, String senderAvatar, String giftUrl, int count, long ts) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.giftUrl = giftUrl;
        this.count = count;
        this.timestampMs = ts;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GiftEvent)) return false;
        GiftEvent g = (GiftEvent) o;
        return timestampMs == g.timestampMs
                && count == g.count
                && safe(senderName).equals(safe(g.senderName))
                && safe(senderAvatar).equals(safe(g.senderAvatar))
                && safe(giftUrl).equals(safe(g.giftUrl));
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + (int) (timestampMs ^ (timestampMs >>> 32));
        h = 31 * h + count;
        h = 31 * h + safe(senderName).hashCode();
        h = 31 * h + safe(senderAvatar).hashCode();
        h = 31 * h + safe(giftUrl).hashCode();
        return h;
    }

    @NonNull
    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

