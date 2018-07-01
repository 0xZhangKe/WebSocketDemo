package com.zhangke.websocket;

/**
 * Created by ZhangKe on 2018/6/28.
 */
public interface IWebSocketPage extends SocketListener {

    void onServiceBindSuccess();
    void sendText(String text);
    void reconnect();//重新连接 WebSocket
}
