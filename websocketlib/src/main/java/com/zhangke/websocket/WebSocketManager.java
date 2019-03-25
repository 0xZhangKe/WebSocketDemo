package com.zhangke.websocket;

import com.zhangke.websocket.dispatcher.InnerResponseDelivery;
import com.zhangke.websocket.dispatcher.ResponseProcessEngine;
import com.zhangke.websocket.dispatcher.UserResponseDelivery;
import com.zhangke.websocket.request.ByteArrayRequest;
import com.zhangke.websocket.request.ByteBufferRequest;
import com.zhangke.websocket.request.Request;
import com.zhangke.websocket.request.StringRequest;
import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;
import com.zhangke.websocket.dispatcher.ResponseDelivery;
import com.zhangke.websocket.util.TextUtil;

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

    /**
     * 下游用户注册的监听器集合
     */
    private ResponseDelivery mDelivery = new UserResponseDelivery();
    private ResponseDelivery mInnerDelivery = new InnerResponseDelivery();

    public WebSocketManager(WebSocketSetting setting) {
        this.mSetting = setting;
        //todo 需要同时支持 Java 端以及 Android 端

        mWebSocket = new WebSocketWrapper(this.mSetting, getSocketWrapperListener());

    }

    private SocketWrapperListener getSocketWrapperListener() {
        return new SocketWrapperListener() {
            @Override
            public void onConnected() {
                mSetting.getResponseDispatcher()
                        .onConnected(mDelivery);
                if (!mInnerDelivery.isEmpty()) {
                    mInnerDelivery.onConnected();
                }
            }

            @Override
            public void onConnectFailed(Throwable e) {
                //如果内部监听器不为空，则将连接失败事件拦截
                //todo 此处需要重新设计，这样做耦合度太高
                if (mInnerDelivery.isEmpty()) {
                    mSetting.getResponseDispatcher()
                            .onConnectError(e, mDelivery);
                } else {
                    mInnerDelivery.onConnectError(e);
                }
            }

            @Override
            public void onDisconnect() {
                mSetting.getResponseDispatcher()
                        .onDisconnected(mDelivery);
                if (!mInnerDelivery.isEmpty()) {
                    mInnerDelivery.onDisconnected();
                }
            }

            @Override
            public void onSendDataError(Request request, int type, Throwable tr) {
                ErrorResponse errorResponse = ErrorResponse.build(request, type, tr);
                if (mSetting.processDataOnBackground()) {
                    ResponseProcessEngine.getInstance()
                            .onSendDataError(errorResponse,
                                    mSetting.getResponseDispatcher(),
                                    mDelivery);
                } else {
                    mSetting.getResponseDispatcher().onSendMessageError(errorResponse, mDelivery);
                }
                if (!mInnerDelivery.isEmpty()) {
                    mInnerDelivery.onSendMessageError(errorResponse);
                }
                //todo 使用完注意释放资源 request.release();
            }

            @Override
            public void onReceivedData(Response response) {
                if (mSetting.processDataOnBackground()) {
                    ResponseProcessEngine.getInstance()
                            .onMessageReceive(response,
                                    mSetting.getResponseDispatcher(),
                                    mDelivery);
                } else {
                    mSetting.getResponseDispatcher()
                            .onMessageResponse(response,
                                    mDelivery);
                }
                if (!mInnerDelivery.isEmpty()) {
                    mInnerDelivery.onMessageResponse(response);
                }
                //todo 使用完注意释放资源 request.release();
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
        if (TextUtil.isEmpty(text)) {
            return;
        }
        StringRequest request = StringRequest.obtain();
        request.setRequestData(text);
        sendRequest(request);
    }

    /**
     * 发送数据
     */
    public void send(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
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
        mDelivery.addListener(listener);
        return this;
    }

    public WebSocketManager removeListener(SocketListener listener) {
        mDelivery.removeListener(listener);
        return this;
    }

    public WebSocketManager addInnerListener(SocketListener listener) {
        mInnerDelivery.addListener(listener);
        return this;
    }

    public WebSocketManager removeInnerListener(SocketListener listener) {
        mInnerDelivery.removeListener(listener);
        return this;
    }

    /**
     * 获取配置类，
     * 部分参数支持动态设定。
     */
    public WebSocketSetting getSetting() {
        return mSetting;
    }

    /**
     * 销毁该连接
     */
    public void destroy() {

    }
}
