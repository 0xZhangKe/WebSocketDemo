package com.zhangke.websocket;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * 已经绑定了 WebSocketService 服务的 Activity，
 * <p>
 * Created by ZhangKe on 2018/6/25.
 */
public abstract class AbsWebSocketActivity extends AppCompatActivity implements IWebSocketPage {

    protected final String LOGTAG = this.getClass().getSimpleName();

    private WebSocketServiceConnectManager mConnectManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectManager = new WebSocketServiceConnectManager(this, this);
        mConnectManager.onCreate();
    }

    @Override
    public void sendText(String text) {
        mConnectManager.sendText(text);
    }

    @Override
    public void reconnect() {
        mConnectManager.reconnect();
    }

    /**
     * 服务绑定成功时的回调，可以在此初始化数据
     */
    @Override
    public void onServiceBindSuccess() {

    }

    /**
     * WebSocket 连接成功事件
     */
    @Override
    public void onConnected() {

    }

    /**
     * WebSocket 连接出错事件
     *
     * @param cause 出错原因
     */
    @Override
    public void onConnectError(Throwable cause) {

    }

    /**
     * WebSocket 连接断开事件
     */
    @Override
    public void onDisconnected() {

    }

    @Override
    protected void onDestroy() {
        mConnectManager.onDestroy();
        super.onDestroy();
    }
}
