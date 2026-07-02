package com.codder.ultimate.live.utils;

public interface LiveHandler {

    void onGift(Object[] args);

    void onComment(Object[] args);

    void onView(Object[] args);

    void onBlock(Object[] args);

    void onAnimationFilter(Object[] args);

    void onSimpleFilter(Object[] args);

    void onGif(Object[] args);

    void onLiveEndByEnd(Object[] args);

    void onPkRequest(Object[] args);

    void onPkRequestAnswer(Object[] args);

    void onPkEnd(Object[] args);

    void onHostLiveEnd(Object[] args);

    void onSingleLiveUser(Object[] args);

    void onGetUser(Object[] args);

    void onUserCoinUpdate(Object[] args);

    void onBlockUserAlert(Object[] args);

    void onBanned(Object[] args);

    void onBannedUserList(Object[] args);

    void onTotalRoomCoins(Object[] args);

    void onHostDetailsForAudience(Object[] args);

    void onGame(Object[] args);

    void onBroadcastNotification(Object[] args);
}
