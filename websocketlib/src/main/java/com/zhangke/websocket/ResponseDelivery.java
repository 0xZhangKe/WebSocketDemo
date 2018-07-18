package com.zhangke.websocket;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息发射器，
 * 内部维护一个 {@link SocketListener} 的 List，
 * 调用每一个方法都会通知 List 中所有的 Listener，
 * 这么做主要为了简化代码。
 * Created by ZhangKe on 2018/6/26.
 */
public class ResponseDelivery implements SocketListener {

    private final List<SocketListener> mSocketListenerList = new ArrayList<>();

    public ResponseDelivery() {
    }

    public void addListener(SocketListener listener) {
        synchronized (mSocketListenerList) {
            mSocketListenerList.add(listener);
        }
    }

    public void removeListener(SocketListener listener) {
        synchronized (mSocketListenerList) {
            mSocketListenerList.remove(listener);
        }
    }

    @Override
    public void onConnected() {
        synchronized (mSocketListenerList) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onConnected();
            }
        }
    }

    @Override
    public void onConnectError(Throwable cause) {
        synchronized (mSocketListenerList) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onConnectError(cause);
            }
        }
    }

    @Override
    public void onDisconnected() {
        synchronized (mSocketListenerList) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onDisconnected();
            }
        }
    }

    @Override
    public void onMessageResponse(Response message) {
        synchronized (mSocketListenerList) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onMessageResponse(message);
            }
        }
    }

    @Override
    public void onSendMessageError(ErrorResponse message) {
        synchronized (mSocketListenerList) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onSendMessageError(message);
            }
        }
    }
}
