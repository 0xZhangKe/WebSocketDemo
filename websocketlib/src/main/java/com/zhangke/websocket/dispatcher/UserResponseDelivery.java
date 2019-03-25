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
public class UserResponseDelivery implements ResponseDelivery {

    /**
     * Listener 操作锁
     */
    static final Object LISTENER_BLOCK = new Object();

    private Queue<ResponseCallbackRunnable> RESPONSE_RUNNABLE_POOL;
    private Queue<ErrorResponseCallbackRunnable> ERROR_RUNNABLE_POOL;

    private final List<SocketListener> mSocketListenerList = new ArrayList<>();

    public UserResponseDelivery() {
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
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (LISTENER_BLOCK) {
                        for (SocketListener listener : mSocketListenerList) {
                            listener.onConnected();
                        }
                    }
                }
            });
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
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (LISTENER_BLOCK) {
                        for (SocketListener listener : mSocketListenerList) {
                            listener.onConnectError(cause);
                        }
                    }
                }
            });
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
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (LISTENER_BLOCK) {
                        for (SocketListener listener : mSocketListenerList) {
                            listener.onDisconnected();
                        }
                    }
                }
            });
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
            runOnMainThread(getResponseRunnable(message));
        }
    }

    private Runnable getResponseRunnable(final Response message) {
        if (RESPONSE_RUNNABLE_POOL == null) {
            RESPONSE_RUNNABLE_POOL = new ArrayDeque<>(7);
        }
        ResponseCallbackRunnable runnable = RESPONSE_RUNNABLE_POOL.poll();
        if (runnable == null) {
            runnable = new ResponseCallbackRunnable();
        }
        runnable.setResponse(message);
        runnable.setSocketListenerList(mSocketListenerList);
        return runnable;
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
            runOnMainThread(getErrorResponseRunnable(message));
        }
    }

    private Runnable getErrorResponseRunnable(final ErrorResponse message) {
        if (ERROR_RUNNABLE_POOL == null) {
            ERROR_RUNNABLE_POOL = new ArrayDeque<>(7);
        }
        ErrorResponseCallbackRunnable response = ERROR_RUNNABLE_POOL.poll();
        if (response == null) {
            response = new ErrorResponseCallbackRunnable();
        }
        response.setErrorResponse(message);
        response.setSocketListenerList(mSocketListenerList);
        return response;
    }

    @Override
    public boolean isEmpty() {
        return mSocketListenerList.isEmpty();
    }
}
