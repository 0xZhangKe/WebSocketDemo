package com.zhangke.websocket.dispatcher;

import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;
import com.zhangke.websocket.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息发射器，用于框架内部使用
 * <p>
 * Created by ZhangKe on 2019/3/25.
 */
public class InnerResponseDelivery implements ResponseDelivery {

    private static final String TAG = "InnerResponseDelivery";

    private final List<SocketListener> mSocketListenerList = new ArrayList<>();

    @Override
    public void addListener(SocketListener listener) {
        if (listener == null) {
            return;
        }
        if (!mSocketListenerList.contains(listener)) {
            synchronized (mSocketListenerList) {
                mSocketListenerList.add(listener);
            }
        }
    }

    @Override
    public void removeListener(SocketListener listener) {
        if (listener == null) {
            return;
        }
        if (mSocketListenerList.contains(listener)) {
            synchronized (mSocketListenerList) {
                mSocketListenerList.remove(listener);
            }
        }
    }

    @Override
    public void onConnected() {
        if (isEmpty()) {
            return;
        }
        synchronized (mSocketListenerList) {
            for (SocketListener item : mSocketListenerList) {
                item.onConnected();
            }
        }
    }

    @Override
    public void onConnectError(Throwable cause) {
        if (isEmpty()) {
            return;
        }
        synchronized (mSocketListenerList) {
            for (SocketListener item : mSocketListenerList) {
                item.onConnectError(cause);
            }
        }
    }

    @Override
    public void onDisconnected() {
        if (isEmpty()) {
            return;
        }
        synchronized (mSocketListenerList) {
            for (SocketListener item : mSocketListenerList) {
                item.onDisconnected();
            }
        }
    }

    @Override
    public void onMessageResponse(Response message) {
        if (isEmpty() || message == null) {
            return;
        }
        synchronized (mSocketListenerList) {
            try {
                for (SocketListener item : mSocketListenerList) {
                    item.onMessageResponse(message);
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "onMessageResponse(Response)", e);
            }
        }
    }

    @Override
    public void onSendMessageError(ErrorResponse error) {
        if (isEmpty() || error == null) {
            return;
        }
        synchronized (mSocketListenerList) {
            try {
                for (SocketListener item : mSocketListenerList) {
                    item.onSendMessageError(error);
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "onSendMessageError(ErrorResponse)", e);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return mSocketListenerList.isEmpty();
    }
}
