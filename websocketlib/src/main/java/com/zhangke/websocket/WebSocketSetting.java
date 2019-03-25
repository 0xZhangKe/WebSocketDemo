package com.zhangke.websocket;

import com.zhangke.websocket.dispatcher.DefaultResponseDispatcher;
import com.zhangke.websocket.dispatcher.IResponseDispatcher;

import org.java_websocket.drafts.Draft;

import java.net.Proxy;

/**
 * WebSocket 使用配置
 * Created by ZhangKe on 2018/6/26.
 */
public class WebSocketSetting {

    /**
     * WebSocket 连接地址
     */
    private String connectUrl;
    /**
     * 消息处理分发器
     */
    private IResponseDispatcher responseProcessDelivery;
    /**
     * 设置是否使用子线程处理数据，
     * true-接收到消息后将使用子线程处理数据，
     * false-反之。
     * 默认为 true
     */
    private boolean processDataOnBackground;
    /**
     * 设置网络连接变化后是否自动重连。</br>
     * 如果设置 true 则需要注册广播：{@link NetworkChangedReceiver}，</br>
     * 并添加 ACCESS_NETWORK_STATE 权限。
     */
    private boolean reconnectWithNetworkChanged;
    /**
     * 代理
     */
    private Proxy mProxy;
    /**
     * 协议实现，默认为 {@link org.java_websocket.drafts.Draft_6455}，
     * 框架也只提供了这一个实现，一般情况不需要设置。
     * 特殊需求可以自定义继承 {@link Draft} 的类
     */
    private Draft draft;
    /**
     * 重连次数，默认为：10 次
     */
    private int reconnectFrequency = 10;

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

    public void setReconnectWithNetworkChanged(boolean reconnectWithNetworkChanged) {
        this.reconnectWithNetworkChanged = reconnectWithNetworkChanged;
    }

    public Proxy getProxy() {
        return mProxy;
    }

    public void setProxy(Proxy mProxy) {
        this.mProxy = mProxy;
    }

    public Draft getDraft() {
        return draft;
    }

    public void setDraft(Draft draft) {
        this.draft = draft;
    }

    public boolean processDataOnBackground() {
        return processDataOnBackground;
    }

    public void setProcessDataOnBackground(boolean processDataOnBackground) {
        this.processDataOnBackground = processDataOnBackground;
    }

    public int getReconnectFrequency() {
        return reconnectFrequency;
    }

    public void setReconnectFrequency(int reconnectFrequency) {
        this.reconnectFrequency = reconnectFrequency;
    }
}
