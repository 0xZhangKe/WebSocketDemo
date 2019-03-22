package com.zhangke.websocket;

import java.net.Proxy;

/**
 * WebSocket 使用配置
 * Created by ZhangKe on 2018/6/26.
 */
public class WebSocketSetting {

    private String connectUrl;
    private IResponseDispatcher responseProcessDelivery;
    private boolean reconnectWithNetworkChanged;
    private Proxy mProxy;

    /**
     * 获取 WebSocket 链接地址
     */
    public String getConnectUrl() {
        return this.connectUrl;
    }

    /**
     * 设置 WebSocket 链接地址，第一次设置有效，
     * 必须在启动 WebSocket 线程之前设置
     */
    public void setConnectUrl(String connectUrl) {
        this.connectUrl = connectUrl;
    }

    /**
     * 获取当前已设置的消息分发器
     */
    public IResponseDispatcher getResponseDispatcher() {
        if (responseProcessDelivery == null) {
            responseProcessDelivery = new DefaultResponseDispatcher();
        }
        return responseProcessDelivery;
    }

    /**
     * 设置消息分发器
     */
    public void setResponseProcessDelivery(IResponseDispatcher responseProcessDelivery) {
        this.responseProcessDelivery = responseProcessDelivery;
    }

    public boolean isReconnectWithNetworkChanged() {
        return this.reconnectWithNetworkChanged;
    }

    /**
     * 设置网络连接变化后是否自动重连。</br>
     * 如果设置 true 则需要注册广播：{@link NetworkChangedReceiver}，</br>
     * 并添加 ACCESS_NETWORK_STATE 权限。
     */
    public void setReconnectWithNetworkChanged(boolean reconnectWithNetworkChanged) {
        this.reconnectWithNetworkChanged = reconnectWithNetworkChanged;
    }

    public Proxy getProxy() {
        return mProxy;
    }

    public void setProxy(Proxy mProxy) {
        this.mProxy = mProxy;
    }
}
