package com.codder.ultimate.socket;

public interface CallHandler {

    void onCallRequest(Object[] args);

    void onCallConfirm(Object[] args);

    void onCallAnswer(Object[] args);

    void onCallReceive(Object[] args);

    void onCallCancel(Object[] args);

    void chatOrCallGiftSent(Object[] args);

}
