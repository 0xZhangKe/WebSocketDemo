package com.zhangke.websocket.dispatcher;

import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;

import org.java_websocket.framing.Framedata;

import java.nio.ByteBuffer;

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
        delivery.onConnectFailed(cause);
    }

    @Override
    public void onDisconnected(ResponseDelivery delivery) {
        delivery.onDisconnect();
    }

    @Override
    public void onMessageResponse(String message, ResponseDelivery delivery) {
        delivery.onMessage(message);
    }

    @Override
    public void onMessageResponse(ByteBuffer byteBuffer, ResponseDelivery delivery) {
        delivery.onMessage(byteBuffer);
    }

    @Override
    public void onPing(Framedata framedata, ResponseDelivery delivery) {
        delivery.onPing(framedata);
    }

    @Override
    public void onPong(Framedata framedata, ResponseDelivery delivery) {
        delivery.onPong(framedata);
    }

    @Override
    public void onSendMessageError(ErrorResponse error, ResponseDelivery delivery) {
        delivery.onSendDataError(error);
    }
}
