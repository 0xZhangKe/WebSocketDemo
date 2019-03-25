package com.zhangke.websocket.dispatcher;

import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.response.ErrorResponse;

import java.util.List;

/**
 * 用于执行 {@link ErrorResponse} 响应的可复用 Runnable 对象
 * <p>
 * Created by ZhangKe on 2019/3/25.
 */
public class ErrorResponseCallbackRunnable implements Runnable {

    private ErrorResponse errorResponse;
    private List<SocketListener> socketListenerList;

    void setErrorResponse(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    void setSocketListenerList(List<SocketListener> socketListenerList) {
        this.socketListenerList = socketListenerList;
    }

    @Override
    public void run() {
        synchronized (ResponseDelivery.LISTENER_BLOCK) {
            for (SocketListener listener : socketListenerList) {
                listener.onSendMessageError(errorResponse);
            }
        }
    }
}
