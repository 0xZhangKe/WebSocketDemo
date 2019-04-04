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
    public void onConnectFailed(Throwable cause, ResponseDelivery delivery) {
        delivery.onConnectFailed(cause);
    }

    @Override
    public void onDisconnect(ResponseDelivery delivery) {
        delivery.onDisconnect();
    }

    @Override
    public void onMessage(String message, ResponseDelivery delivery) {
        delivery.onMessage(message, null);
    }

    @Override
    public void onMessage(ByteBuffer byteBuffer, ResponseDelivery delivery) {
        delivery.onMessage(byteBuffer, null);
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
    public void onSendDataError(ErrorResponse error, ResponseDelivery delivery) {
        delivery.onSendDataError(error);
    }
}
