package com.zhangke.websocket;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 绑定了 WebSocketService 的抽象 Fragment
 * Created by ZhangKe on 2018/6/28.
 */
public abstract class AbsWebSocketFragment extends Fragment implements IWebSocketPage {

    protected final String LOGTAG = this.getClass().getSimpleName();

    private WebSocketServiceConnectManager mConnectManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mConnectManager = new WebSocketServiceConnectManager(getActivity().getApplicationContext(), this);
        mConnectManager.onCreate();
        return super.onCreateView(inflater, container, savedInstanceState);
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
    public void onDestroy() {
        mConnectManager.onDestroy();
        super.onDestroy();
    }
}
