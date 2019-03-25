package com.zhangke.websocket.dispatcher;

import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.response.Response;

import java.util.List;

/**
 * 用于执行 {@link Response} 响应的可复用 Runnable 对象
 * Created by ZhangKe on 2019/3/25.
 */
public class ResponseCallbackRunnable implements Runnable {

    private Response response;
    private List<SocketListener> socketListenerList;

    void setResponse(Response response) {
        this.response = response;
    }

    void setSocketListenerList(List<SocketListener> socketListenerList) {
        this.socketListenerList = socketListenerList;
    }

    @Override
    public void run() {
        synchronized (ResponseDelivery.LISTENER_BLOCK) {
            for (SocketListener listener : socketListenerList) {
                listener.onMessageResponse(response);
            }
        }
    }
}
