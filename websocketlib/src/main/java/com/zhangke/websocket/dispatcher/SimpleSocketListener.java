package com.zhangke.websocket.dispatcher;

import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.response.ErrorResponse;

import org.java_websocket.framing.Framedata;

import java.nio.ByteBuffer;

/**
 * 简单的 SocketListener
 * <p>
 * Created by ZhangKe on 2019/3/25.
 */
public class SimpleSocketListener implements SocketListener {

    @Override
    public void onConnected() {
        // to override
    }

    @Override
    public void onConnectFailed(Throwable e) {
        // to override
    }

    @Override
    public void onDisconnect() {
        // to override
    }

    @Override
    public void onSendDataError(ErrorResponse errorResponse) {
        // to override
    }

    @Override
    public void onMessage(String message) {
        // to override
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        // to override
    }

    @Override
    public void onPing(Framedata framedata) {
        // to override
    }

    @Override
    public void onPong(Framedata framedata) {
        // to override
    }
}
