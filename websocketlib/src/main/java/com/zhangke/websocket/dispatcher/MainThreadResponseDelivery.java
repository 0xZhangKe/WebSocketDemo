package com.zhangke.websocket.dispatcher;

import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static com.zhangke.websocket.util.ThreadUtil.checkMainThread;
import static com.zhangke.websocket.util.ThreadUtil.runOnMainThread;

/**
 * 用户注册的消息发射器,
 * 内部维护一个 {@link SocketListener} 的 List，
 * 调用每一个方法都会通知 List 中所有的 Listener，
 * 这么做主要为了统一控制消息的回调线程以及简化代码。
 * <p>
 * Created by ZhangKe on 2019/3/25.
 */
public class MainThreadResponseDelivery implements ResponseDelivery {

    /**
     * Listener 操作锁
     */
    private static final Object LISTENER_BLOCK = new Object();

    private static Queue<CallbackRunnable> RUNNABLE_POOL;

    private final List<SocketListener> mSocketListenerList = new ArrayList<>();

    public MainThreadResponseDelivery() {
    }

    public void addListener(SocketListener listener) {
        if (listener == null) {
            return;
        }
        if (!mSocketListenerList.contains(listener)) {
            synchronized (LISTENER_BLOCK) {
                mSocketListenerList.add(listener);
            }
        }
    }

    public void removeListener(SocketListener listener) {
        if (listener == null || isEmpty()) {
            return;
        }
        if (mSocketListenerList.contains(listener)) {
            synchronized (LISTENER_BLOCK) {
                mSocketListenerList.remove(listener);
            }
        }
    }

    @Override
    public void onConnected() {
        if (isEmpty()) {
            return;
        }
        if (checkMainThread()) {
            synchronized (LISTENER_BLOCK) {
                for (SocketListener listener : mSocketListenerList) {
                    listener.onConnected();
                }
            }
        } else {
            CallbackRunnable callbackRunnable = getRunnable();
            callbackRunnable.type = 0;
            runOnMainThread(callbackRunnable);
        }
    }

    @Override
    public void onConnectError(final Throwable cause) {
        if (isEmpty()) {
            return;
        }
        if (checkMainThread()) {
            synchronized (LISTENER_BLOCK) {
                for (SocketListener listener : mSocketListenerList) {
                    listener.onConnectError(cause);
                }
            }
        } else {
            CallbackRunnable callbackRunnable = getRunnable();
            callbackRunnable.type = 1;
            callbackRunnable.connectErrorCause = cause;
            runOnMainThread(callbackRunnable);
        }
    }

    @Override
    public void onDisconnected() {
        if (isEmpty()) {
            return;
        }
        if (checkMainThread()) {
            synchronized (LISTENER_BLOCK) {
                for (SocketListener listener : mSocketListenerList) {
                    listener.onDisconnected();
                }
            }
        } else {
            CallbackRunnable callbackRunnable = getRunnable();
            callbackRunnable.type = 2;
            runOnMainThread(callbackRunnable);
        }
    }

    @Override
    public void onMessageResponse(final Response message) {
        if (isEmpty() || message == null) {
            return;
        }
        if (checkMainThread()) {
            synchronized (LISTENER_BLOCK) {
                for (SocketListener listener : mSocketListenerList) {
                    listener.onMessageResponse(message);
                }
            }
        } else {
            CallbackRunnable callbackRunnable = getRunnable();
            callbackRunnable.type = 3;
            callbackRunnable.response = message;
            runOnMainThread(callbackRunnable);
        }
    }

    @Override
    public void onSendMessageError(final ErrorResponse message) {
        if (isEmpty() || message == null) {
            return;
        }
        if (checkMainThread()) {
            synchronized (LISTENER_BLOCK) {
                for (SocketListener listener : mSocketListenerList) {
                    listener.onSendMessageError(message);
                }
            }
        } else {
            CallbackRunnable callbackRunnable = getRunnable();
            callbackRunnable.type = 4;
            callbackRunnable.errorResponse = message;
            runOnMainThread(callbackRunnable);
        }
    }

    private CallbackRunnable getRunnable() {
        if (RUNNABLE_POOL == null) {
            RUNNABLE_POOL = new ArrayDeque<>(5);
        }
        CallbackRunnable runnable = RUNNABLE_POOL.poll();
        if (runnable == null) {
            runnable = new CallbackRunnable();
        }
        return runnable;
    }

    @Override
    public boolean isEmpty() {
        return mSocketListenerList.isEmpty();
    }

    private static class CallbackRunnable implements Runnable {

        List<SocketListener> mSocketListenerList = new ArrayList<>();
        Response response;
        ErrorResponse errorResponse;
        Throwable connectErrorCause;
        /**
         * 0-连接成功；
         * 1-连接失败；
         * 2-连接断开；
         * 3-接收到数据；
         * 4-数据发送失败
         */
        int type = -1;

        @Override
        public void run() {
            try {
                if (type == -1 ||
                        mSocketListenerList == null ||
                        mSocketListenerList.isEmpty()) {
                    return;
                }
                if (type == 1 && connectErrorCause == null) return;
                if (type == 3 && response == null) return;
                if (type == 4 && errorResponse == null) return;
                synchronized (LISTENER_BLOCK) {
                    switch (type) {
                        case 0:
                            response = null;
                            errorResponse = null;
                            connectErrorCause = null;
                            for (SocketListener listener : mSocketListenerList) {
                                listener.onConnected();
                            }
                            break;
                        case 1:
                            response = null;
                            errorResponse = null;
                            for (SocketListener listener : mSocketListenerList) {
                                listener.onConnectError(connectErrorCause);
                            }
                            connectErrorCause = null;
                            break;
                        case 2:
                            response = null;
                            errorResponse = null;
                            connectErrorCause = null;
                            for (SocketListener listener : mSocketListenerList) {
                                listener.onDisconnected();
                            }
                            break;
                        case 3:
                            errorResponse = null;
                            connectErrorCause = null;
                            for (SocketListener listener : mSocketListenerList) {
                                listener.onMessageResponse(response);
                            }
                            response = null;
                            break;
                        case 4:
                            response = null;
                            connectErrorCause = null;
                            for (SocketListener listener : mSocketListenerList) {
                                listener.onSendMessageError(errorResponse);
                            }
                            errorResponse = null;
                            break;
                    }
                    mSocketListenerList = null;
                }
            } finally {
                RUNNABLE_POOL.offer(this);
            }
        }
    }
}
