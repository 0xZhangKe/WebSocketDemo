package com.zhangke.websocket;

import java.util.List;

/**
 * Created by ZhangKe on 2018/6/26.
 */
public class CommonResponseDispacther implements IResponseDispatcher {

    private ResponseDelivery mResponseDelivery;

    public CommonResponseDispacther(ResponseDelivery mResponseDelivery) {
        this.mResponseDelivery = mResponseDelivery;
    }

    @Override
    public void onConnected() {
        mResponseDelivery.onConnected();
    }

    @Override
    public void onConnectError(Throwable cause) {
        mResponseDelivery.onConnectError(cause);
    }

    @Override
    public void onDisconnected() {
        mResponseDelivery.onDisconnected();
    }

    @Override
    public void onMessageResponse(String message) {
        mResponseDelivery.onMessageResponse(message);
    }

    @Override
    public void onSendMessageError(ErrorResponse error) {
        mResponseDelivery.onSendMessageError(error);
    }
}
