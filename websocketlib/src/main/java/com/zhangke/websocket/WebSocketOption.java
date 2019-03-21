package com.zhangke.websocket;

import java.nio.ByteBuffer;

/**
 * 针对 WebSocket 的操作
 *
 * Created by ZhangKe on 2019/3/21.
 */
public interface WebSocketOption {

    void send(String text);
    void send(byte[] bytes);
    void send(ByteBuffer byteBuffer);
    void connect();
}
