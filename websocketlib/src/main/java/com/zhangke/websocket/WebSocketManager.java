package com.zhangke.websocket;

import com.zhangke.websocket.request.ByteArrayRequest;
import com.zhangke.websocket.request.ByteBufferRequest;
import com.zhangke.websocket.request.Request;
import com.zhangke.websocket.request.StringRequest;
import com.zhangke.websocket.response.Response;
import com.zhangke.websocket.response.ResponseDelivery;

import java.nio.ByteBuffer;

/**
 * WebSocket 管理类
 * <p>
 * Created by ZhangKe on 2019/3/21.
 */
public class WebSocketManager {

    private static final String TAG = "WebSocketManager";

    private WebSocketSetting mSetting;

    private WebSocketWrapper mWebSocket;

    private ResponseDelivery delivery = new ResponseDelivery();

    public WebSocketManager(WebSocketSetting setting) {
        this.mSetting = setting;

        mWebSocket = new WebSocketWrapper(this.mSetting, getSocketWrapperListener());
    }

    private SocketWrapperListener getSocketWrapperListener() {
        return new SocketWrapperListener() {
            @Override
            public void onConnected() {
                mSetting.getResponseDispatcher().onConnected(delivery);
            }

            @Override
            public void onConnectFailed(Throwable e) {
                mSetting.getResponseDispatcher().onConnected(delivery);
            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onSendDataError(Request request, int type, Throwable tr) {

            }

            @Override
            public void onReceivedData(Response response) {

            }
        };
    }

    /**
     * 重新连接
     */
    public WebSocketManager reconnect() {
        return this;
    }

    /**
     * 重新连接
     */
    public WebSocketManager reconnect(WebSocketSetting setting) {
        this.mSetting = setting;
        return this;
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
        StringRequest request = StringRequest.obtain();
        request.setRequestData(text);
        sendRequest(request);
    }

    /**
     * 发送数据
     */
    public void send(byte[] bytes) {
        ByteArrayRequest request = ByteArrayRequest.obtain();
        request.setRequestData(bytes);
        sendRequest(request);
    }

    /**
     * 发送数据
     */
    public void send(ByteBuffer byteBuffer) {
        ByteBufferRequest request = ByteBufferRequest.obtain();
        request.setRequestData(byteBuffer);
        sendRequest(request);
    }

    private void sendRequest(Request request) {
        mWebSocket.send(request);
    }

    public WebSocketManager addListener(SocketListener listener) {
        delivery.addListener(listener);
        return this;
    }

    public WebSocketManager removeListener(SocketListener listener) {
        delivery.removeListener(listener);
        return this;
    }

    /**
     * 销毁该连接
     */
    public void destroy() {

    }
}
