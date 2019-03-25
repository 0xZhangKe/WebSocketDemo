package com.zhangke.websocket.dispatcher;

import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;

/**
 * 简单的 SocketListener
 *
 * Created by ZhangKe on 2019/3/25.
 */
public class SimpleSocketListener implements SocketListener {

    @Override
    public void onConnected() {
        //to override
    }

    @Override
    public void onConnectError(Throwable cause) {
        //to override
    }

    @Override
    public void onDisconnected() {
        //to override
    }

    @Override
    public void onMessageResponse(Response message) {
        //to override
    }

    @Override
    public void onSendMessageError(ErrorResponse error) {
        //to override
    }
}
