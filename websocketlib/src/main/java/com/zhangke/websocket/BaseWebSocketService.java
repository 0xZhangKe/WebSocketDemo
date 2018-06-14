package com.zhangke.websocket;

import android.app.Service;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket基础服务
 * Created by ZhangKe on 2018/6/13.
 */
public abstract class BaseWebSocketService extends Service implements SocketListener, ThreadStartListener {

    /**
     * 获取 WebSocket 连接地址
     */
    protected abstract String getConnectUrl();

    private WebSocketThread mWebSocketThread;
    private Handler mHandler;

    private List<SocketListener> mSocketListenerList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mWebSocketThread = new WebSocketThread(getConnectUrl());
        mWebSocketThread.setSocketListener(this);
        mWebSocketThread.setThreadStartListener(this);
        mWebSocketThread.start();
    }

    public void addListener(SocketListener listener) {
        mSocketListenerList.add(listener);
    }

    public void removeListener(SocketListener listener) {
        mSocketListenerList.remove(listener);
    }

    @Override
    public void started() {
        mHandler = mWebSocketThread.getHandler();
        mHandler.sendEmptyMessage(MessageType.CONNECT);
    }

    @Override
    public void onConnected() {
        if (!mSocketListenerList.isEmpty()) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onConnected();
            }
        }
    }

    @Override
    public void onConnectError(Throwable cause) {
        if (!mSocketListenerList.isEmpty()) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onConnectError(cause);
            }
        }
    }

    @Override
    public void onDisconnected() {
        if (!mSocketListenerList.isEmpty()) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onDisconnected();
            }
        }
    }

    @Override
    public void onTextMessage(String message) {
        if (!mSocketListenerList.isEmpty()) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onTextMessage(message);
            }
        }
    }
}
