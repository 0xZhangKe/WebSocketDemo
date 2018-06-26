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
public class BaseWebSocketService extends Service implements SocketListener {

    private WebSocketThread mWebSocketThread;

    private ResponseDelivery mResponseDelivery = new ResponseDelivery();

    private IResponseDispatcher responseDispatcher;

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
        mWebSocketThread = new WebSocketThread(WebSocketSetting.getConnectUrl());
        mWebSocketThread.setSocketListener(this);
        mWebSocketThread.start();

        responseDispatcher = WebSocketSetting.getResponseProcessDelivery();
    }

    @Override
    public void onDestroy() {
        mWebSocketThread.getHandler().sendEmptyMessage(MessageType.QUIT);
        super.onDestroy();
    }

    public void sendText(String text) {
        if (mWebSocketThread.getHandler() == null) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorCode(10);
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

    public void addListener(SocketListener listener) {
        mResponseDelivery.addListener(listener);
    }

    public void removeListener(SocketListener listener) {
        mResponseDelivery.removeListener(listener);
    }

    @Override
    public void onConnected() {
        mResponseDelivery.onConnected();
    }

    @Override
    public void onConnectError(Throwable cause) {
        responseDispatcher.onConnectError(cause);
    }

    @Override
    public void onDisconnected() {
        responseDispatcher.onDisconnected();
    }

    @Override
    public void onMessageResponse(String message) {
        responseDispatcher.onMessageResponse(message);
    }

    @Override
    public void onSendMessageError(ErrorResponse message) {
        responseDispatcher.onSendMessageError(message);
    }
}
