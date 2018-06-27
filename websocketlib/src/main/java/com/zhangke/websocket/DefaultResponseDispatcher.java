package com.zhangke.websocket;

/**
 * 通用消息调度器，没做任何数据处理
 * Created by ZhangKe on 2018/6/26.
 */
public class DefaultResponseDispatcher implements IResponseDispatcher {

    @Override
    public void onConnected(ResponseDelivery delivery) {
        delivery.onConnected();
    }

    @Override
    public void onConnectError(Throwable cause, ResponseDelivery delivery) {
        delivery.onConnectError(cause);
    }

    @Override
    public void onDisconnected(ResponseDelivery delivery) {
        delivery.onDisconnected();
    }

    @Override
    public void onMessageResponse(Response message, ResponseDelivery delivery) {
        delivery.onMessageResponse(message);
    }

    @Override
    public void onSendMessageError(ErrorResponse error, ResponseDelivery delivery) {
        delivery.onSendMessageError(error);
    }
}
