package com.zhangke.websocket;

/**
 * WebSocket监听器
 * Created by ZhangKe on 2018/6/8.
 */
public interface SocketListener {

    void onConnected();

    void onConnectError(Throwable cause);

    void onDisconnected();

    void onTextMessage(String message);
}
