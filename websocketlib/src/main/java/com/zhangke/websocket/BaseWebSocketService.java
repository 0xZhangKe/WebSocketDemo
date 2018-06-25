package com.zhangke.websocket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket基础服务
 * Created by ZhangKe on 2018/6/13.
 */
public abstract class BaseWebSocketService extends Service implements SocketListener {

    /**
     * 获取 WebSocket 连接地址
     */
    protected abstract String getConnectUrl();

    private WebSocketThread mWebSocketThread;

    private List<SocketListener> mSocketListenerList = new ArrayList<>();

    private BaseWebSocketService.ServiceBinder serviceBinder = new BaseWebSocketService.ServiceBinder();

    public class ServiceBinder extends Binder {
        public BaseWebSocketService getService() {
            return BaseWebSocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (serviceBinder == null) {
            serviceBinder = new BaseWebSocketService.ServiceBinder();
        }
        return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWebSocketThread = new WebSocketThread(getConnectUrl());
        mWebSocketThread.setSocketListener(this);
        mWebSocketThread.start();
    }

    @Override
    public void onDestroy() {
        mWebSocketThread.getHandler().sendEmptyMessage(MessageType.QUIT);
        super.onDestroy();
    }

    public void sendText(String text) {
        if (mWebSocketThread.getHandler() == null) {
            onSendMessageError(text);
        } else {
            Message message = mWebSocketThread.getHandler().obtainMessage();
            message.obj = text;
            message.what = MessageType.SEND_MESSAGE;
            mWebSocketThread.getHandler().sendMessage(message);
        }
    }

    public void addListener(SocketListener listener) {
        mSocketListenerList.add(listener);
    }

    public void removeListener(SocketListener listener) {
        mSocketListenerList.remove(listener);
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
    public void onMessageResponse(String message) {
        if (!mSocketListenerList.isEmpty()) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onMessageResponse(message);
            }
        }
    }

    @Override
    public void onSendMessageError(String message) {
        if (!mSocketListenerList.isEmpty()) {
            for (SocketListener listener : mSocketListenerList) {
                listener.onSendMessageError(message);
            }
        }
    }
}
