package com.zhangke.websocket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangKe on 2018/6/26.
 */
public class ResponseDelivery implements SocketListener {

    private List<SocketListener> mSocketListenerList = new ArrayList<>();

    public ResponseDelivery() {
    }

    public void addListener(SocketListener listener) {
        mSocketListenerList.add(listener);
    }

    public void removeListener(SocketListener listener) {
        mSocketListenerList.remove(listener);
    }

    @Override
    public void onConnected() {
        for (SocketListener listener : mSocketListenerList) {
            listener.onConnected();
        }
    }

    @Override
    public void onConnectError(Throwable cause) {
        for (SocketListener listener : mSocketListenerList) {
            listener.onConnectError(cause);
        }
    }

    @Override
    public void onDisconnected() {
        for (SocketListener listener : mSocketListenerList) {
            listener.onDisconnected();
        }
    }

    @Override
    public void onMessageResponse(Response message) {
        for (SocketListener listener : mSocketListenerList) {
            listener.onMessageResponse(message);
        }
    }

    @Override
    public void onSendMessageError(ErrorResponse message) {
        for (SocketListener listener : mSocketListenerList) {
            listener.onSendMessageError(message);
        }
    }
}
