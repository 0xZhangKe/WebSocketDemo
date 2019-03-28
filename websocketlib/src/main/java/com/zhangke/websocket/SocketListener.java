package com.zhangke.websocket;

import com.zhangke.websocket.request.Request;
import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;

import org.java_websocket.framing.Framedata;

import java.nio.ByteBuffer;

/**
 * WebSocket 监听器,
 * for user
 * Created by ZhangKe on 2018/6/8.
 */
public interface SocketListener {

    /**
     * 连接成功
     */
    void onConnected();

    /**
     * 连接失败
     */
    void onConnectFailed(Throwable e);

    /**
     * 连接断开
     */
    void onDisconnect();

    /**
     * 数据发送失败
     *
     * @param errorResponse 失败响应
     */
    void onSendDataError(ErrorResponse errorResponse);

    /**
     * 接收到文本消息
     */
    void onMessage(String message);

    /**
     * 接收到二进制消息
     */
    void onMessage(ByteBuffer bytes);

    /**
     * 接收到 ping
     */
    void onPing(Framedata framedata);

    /**
     * 接收到 pong
     */
    void onPong(Framedata framedata);
}
