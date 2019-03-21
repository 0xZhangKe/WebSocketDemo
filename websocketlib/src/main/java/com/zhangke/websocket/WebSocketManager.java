package com.zhangke.websocket;

import java.nio.ByteBuffer;

/**
 * WebSocket 管理类
 * <p>
 * Created by ZhangKe on 2019/3/21.
 */
public class WebSocketManager {

    private static final String TAG = "WebSocketManager";

    private WebSocketSetting mSetting;

    private WebSocketThread mThread;

    public WebSocketManager(WebSocketSetting setting) {
        this.mSetting = setting;
        mThread = new WebSocketThread(this.mSetting);
        mThread.start();
    }

    /**
     * 重新连接
     */
    public void reconnect() {

    }

    /**
     * 重新连接
     */
    public void reconnect(WebSocketSetting setting) {
        this.mSetting = setting;

    }

    /**
     * 断开连接
     */
    public void disConnect() {

    }

    /**
     * 发送数据
     */
    public void send(String text) {

    }

    /**
     * 发送数据
     */
    public void send(byte[] bytes) {

    }

    /**
     * 发送数据
     */
    public void send(ByteBuffer byteBuffer) {

    }

    /**
     * 销毁改连接
     */
    public void destroy() {

    }
}
