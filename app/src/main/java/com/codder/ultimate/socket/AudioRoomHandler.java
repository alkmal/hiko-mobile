package com.codder.ultimate.socket;

public interface AudioRoomHandler {

    void onLiveEndByEnd(Object[] args);

    void onComment(Object[] args);

    void onGift(Object[] args);

    void onView(Object[] args);

    void onAddRequested(Object[] args);

    void onAddParticipants(Object[] args);

    default void onSeatBusy(Object[] args) { }

    void onLessParticipants(Object[] args);

    void onMuteSeat(Object[] args);

    void onLockSeat(Object[] args);

    void onChangeTheme(Object[] args);

    void onSeat(Object[] args);

    void onBlock(Object[] args);

    void onGetUser(Object[] args);

    void onGetUser2(Object[] args);

    void onInvite(Object[] args);

    void onLiveEnd(Object[] args);

    void onReactionReceived(Object[] args1);

    void onRoomNameChange(Object[] args);

    void onRoomImageChange(Object[] args);

    void onUserCoinUpdate(Object[] args);

    void onBanned(Object[] args);

    void onBannedUserList(Object[] args);

    void onBlockUserAlert(Object[] args);

    void onHostEnter(Object[] args);

    void onAudioLiveHostRemove(Object[] args);

    void onTotalRoomCoins(Object[] args);

    void onGame(Object[] args);

    void onBroadcastNotification(Object[] args);
    void onRoomWelcome(Object[] args);

    default void onRoomHistory(Object[] args) { }
}
