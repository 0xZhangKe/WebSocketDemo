package com.zhangke.websocket;

/**
 * 用于处理及分发接收到的消息
 * Created by ZhangKe on 2018/6/26.
 */
public interface IResponseDispatcher {

    /**
     * 连接成功
     */
    void onConnected(ResponseDelivery delivery);

    /**
     * 连接失败
     *
     * @param cause 失败原因
     */
    void onConnectError(Throwable cause, ResponseDelivery delivery);

    /**
     * 连接断开
     */
    void onDisconnected(ResponseDelivery delivery);

    /**
     * 接收到消息
     */
    void onMessageResponse(Response message, ResponseDelivery delivery);

    /**
     * 消息发送失败或接受到错误消息等等
     */
    void onSendMessageError(ErrorResponse error, ResponseDelivery delivery);

}
