package com.zhangke.WebSocket;

/**
 * WebSocketService 提供的接口
 * <p>
 * Created by zk721 on 2018/1/28.
 */

public interface IWebSocket {

    /**
     * 发送数据
     *
     * @param text 需要发送的数据
     */
    void sendText(String text);

    /**
     * 0-未连接
     * 1-正在连接
     * 2-已连接
     */
    int getConnectStatus();

    /**
     * 重新连接
     */
    void reconnect();

    /**
     * 关闭连接
     */
    void stop();
}
