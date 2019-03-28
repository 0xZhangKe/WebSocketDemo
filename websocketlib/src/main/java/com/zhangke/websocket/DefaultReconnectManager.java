package com.zhangke.websocket;

import com.zhangke.websocket.util.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 负责 WebSocket 重连
 * <p>
 * Created by ZhangKe on 2018/6/24.
 */
public class DefaultReconnectManager implements ReconnectManager {

    private static final String TAG = "WebSocketLib";

    /**
     * 重连锁
     */
    private final Object BLOCK = new Object();

    private WebSocketManager mWebSocketManager;
    private OnConnectListener mOnDisconnectListener;

    /**
     * 是否正在重连
     */
    private volatile boolean reconnecting;
    /**
     * 被销毁
     */
    private volatile boolean destroyed;
    /**
     * 是否需要停止重连
     */
    private volatile boolean needStopReconnect = false;
    /**
     * 是否已连接
     */
    private volatile boolean connected = false;

    private final ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    public DefaultReconnectManager(WebSocketManager webSocketManager,
                                   OnConnectListener onDisconnectListener) {
        this.mWebSocketManager = webSocketManager;
        this.mOnDisconnectListener = onDisconnectListener;
        reconnecting = false;
        destroyed = false;
    }

    @Override
    public boolean reconnecting() {
        return reconnecting;
    }

    @Override
    public void startReconnect() {
        if (reconnecting) {
            LogUtil.i(TAG, "Reconnecting, do not call again.");
            return;
        }
        if (destroyed) {
            LogUtil.e(TAG, "ReconnectManager is destroyed!!!");
            return;
        }
        needStopReconnect = false;
        reconnecting = true;
        if (singleThreadPool.isTerminated()) {
            singleThreadPool.execute(getReconnectRunnable());
        } else {
            LogUtil.e(TAG, "重连线程池中有线程未结束，取消本次重连。");
        }
    }

    private Runnable getReconnectRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                if (destroyed || needStopReconnect) {
                    reconnecting = false;
                    return;
                }
                reconnecting = true;
                connected = false;
                try {
                    int count = mWebSocketManager.getSetting().getReconnectFrequency();
                    for (int i = 0; i < count; i++) {
                        LogUtil.i(TAG, String.format("第%s次重连", i + 1));
                        mWebSocketManager.reconnectOnce();
                        try {
                            BLOCK.wait();
                            if (connected) {
                                LogUtil.i(TAG, "reconnectOnce success!");
                                mOnDisconnectListener.onConnected();
                                return;
                            }
                            if (needStopReconnect) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    //重连失败
                    LogUtil.i(TAG, "reconnectOnce failed!");
                    mOnDisconnectListener.onDisconnect();
                } finally {
                    reconnecting = false;
                }
            }
        };
    }

    @Override
    public void stopReconnect() {
        needStopReconnect = true;
        if (singleThreadPool != null) {
            singleThreadPool.shutdownNow();
        }
    }

    @Override
    public void onConnected() {
        connected = true;
        BLOCK.notifyAll();
    }

    @Override
    public void onConnectError(Throwable th) {
        connected = false;
        BLOCK.notifyAll();
    }

    /**
     * 销毁资源，并停止重连
     */
    @Override
    public void destroy() {
        destroyed = true;
        stopReconnect();
        mWebSocketManager = null;
    }
}
