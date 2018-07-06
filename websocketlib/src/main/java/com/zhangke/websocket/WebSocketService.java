package com.zhangke.websocket;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

/**
 * WebSocket 基础服务
 * Created by ZhangKe on 2018/6/13.
 */
public class WebSocketService extends Service implements SocketListener {

    private WebSocketThread mWebSocketThread;

    private ResponseDelivery mResponseDelivery = new ResponseDelivery();

    private IResponseDispatcher responseDispatcher;

    /**
     * 监听网络变化广播是否已注册
     */
    private boolean networkChangedReceiverRegist = false;
    /**
     * 监听网络变化
     */
    private NetworkChangedReceiver networkChangedReceiver;

    private WebSocketService.ServiceBinder serviceBinder = new WebSocketService.ServiceBinder();

    public class ServiceBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (serviceBinder == null) {
            serviceBinder = new WebSocketService.ServiceBinder();
        }
        return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWebSocketThread = new WebSocketThread(WebSocketSetting.getConnectUrl());
        mWebSocketThread.setSocketListener(this);
        mWebSocketThread.start();

        responseDispatcher = WebSocketSetting.getResponseProcessDelivery();

        //绑定监听网络变化广播
        if (WebSocketSetting.isReconnectWithNetworkChanged()) {
            networkChangedReceiver = new NetworkChangedReceiver(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.wifi.STATE_CHANGE");
            registerReceiver(networkChangedReceiver, filter);
            networkChangedReceiverRegist = true;
        }
    }

    @Override
    public void onDestroy() {
        mWebSocketThread.getHandler().sendEmptyMessage(MessageType.QUIT);
        if (networkChangedReceiverRegist && networkChangedReceiver != null) {
            unregisterReceiver(networkChangedReceiver);
        }
        super.onDestroy();
    }

    public void sendText(String text) {
        if (mWebSocketThread.getHandler() == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorCode(3);
            errorResponse.setCause(new Throwable("WebSocket does not initialization!"));
            errorResponse.setRequestText(text);
            onSendMessageError(errorResponse);
        } else {
            Message message = mWebSocketThread.getHandler().obtainMessage();
            message.obj = text;
            message.what = MessageType.SEND_MESSAGE;
            mWebSocketThread.getHandler().sendMessage(message);
        }
    }

    /**
     * 添加一个 WebSocket 事件监听器
     */
    public void addListener(SocketListener listener) {
        mResponseDelivery.addListener(listener);
    }

    /**
     * 移除一个 WebSocket 事件监听器
     */
    public void removeListener(SocketListener listener) {
        mResponseDelivery.removeListener(listener);
    }

    /**
     * 连接 WebSocket
     */
    public void reconnect() {
        if (mWebSocketThread.getHandler() == null) {
            onConnectError(new Throwable("WebSocket dose not ready"));
        } else {
            mWebSocketThread.getHandler().sendEmptyMessage(MessageType.CONNECT);
        }
    }

    @Override
    public void onConnected() {
        responseDispatcher.onConnected(mResponseDelivery);
    }

    @Override
    public void onConnectError(Throwable cause) {
        responseDispatcher.onConnectError(cause, mResponseDelivery);
    }

    @Override
    public void onDisconnected() {
        responseDispatcher.onDisconnected(mResponseDelivery);
    }

    @Override
    public void onMessageResponse(Response message) {
        responseDispatcher.onMessageResponse(message, mResponseDelivery);
    }

    @Override
    public void onSendMessageError(ErrorResponse message) {
        responseDispatcher.onSendMessageError(message, mResponseDelivery);
    }
}
