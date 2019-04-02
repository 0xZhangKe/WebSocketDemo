package com.zhangke.websocket;

import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.util.LogUtil;

import org.java_websocket.framing.Framedata;

import java.nio.ByteBuffer;

/**
 * 一个简单的 WebSocket 监听器，实现了 {@link SocketListener} 接口，
 * 因为 SocketListener 中的方法比较多，所以在此提供了一个简单版本，
 * 只需要实现其中关注的方法即可。
 * <p>
 * Created by ZhangKe on 2019/4/1.
 */
public abstract class SimpleListener implements SocketListener {

    private final String TAG = "SimpleListener";

    @Override
    public void onConnected() {
        LogUtil.i(TAG, "onConnected()");
    }

    @Override
    public void onConnectFailed(Throwable e) {
        LogUtil.e(TAG, "onConnectFailed(Throwable)", e);
    }

    @Override
    public void onDisconnect() {
        LogUtil.i(TAG, "onDisconnect()");
    }

    @Override
    public <T> void onMessage(T data) {
        LogUtil.e(TAG, "onMessage(T)");
    }

    @Override
    public void onMessage(String message) {
        LogUtil.e(TAG, "onMessage(String)");
    }

    @Override
    public void onSendDataError(ErrorResponse errorResponse) {
        LogUtil.e(TAG, "onSendDataError(ErrorResponse)");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        LogUtil.i(TAG, "onMessage(ByteBuffer)");
    }

    @Override
    public void onPing(Framedata framedata) {
        LogUtil.i(TAG, "onPing(Framedata)");
    }

    @Override
    public void onPong(Framedata framedata) {
        LogUtil.i(TAG, "onPong(Framedata)");
    }
}
