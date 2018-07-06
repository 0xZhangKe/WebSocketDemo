package com.zhangke.websocket;

/**
 * 需要使用 WebSocketService 的页面应该事先的接口
 * Created by ZhangKe on 2018/6/28.
 */
public interface IWebSocketPage extends SocketListener {

    /**
     * WebSocketService 绑定成功回调
     */
    void onServiceBindSuccess();

    /**
     * 通过 WebSocketService 发送数据
     *
     * @param text 需要发送的文本数据
     */
    void sendText(String text);

    /**
     * 重新连接，内部已经做了自动重连机制，一般不需要使用此方法
     */
    void reconnect();
}
