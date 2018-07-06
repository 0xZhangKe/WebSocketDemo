package com.zhangke.websocket;

import android.os.Handler;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 负责 WebSocket 重连
 * <p>
 * Created by ZhangKe on 2018/6/24.
 */
public class ReconnectManager {

    private static final String TAG = "WebSocketLib";

    private WebSocketThread mWebSocketThread;

    /**
     * 是否正在重连
     */
    private volatile boolean retrying;
    private volatile boolean destroyed;
    private final ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    ReconnectManager(WebSocketThread mWebSocketThread) {
        this.mWebSocketThread = mWebSocketThread;
        retrying = false;
        destroyed = false;
    }

    /**
     * 开始重新连接，连接方式为每个500ms连接一次，持续十五次。
     */
    synchronized void performReconnect() {
        if (retrying) {
            Log.i(TAG, "正在重连，请勿重复调用。");
        } else {
            retry();
        }
    }

    /**
     * 开始重连
     */
    private synchronized void retry() {
        if (!retrying) {
            retrying = true;
            synchronized (singleThreadPool) {
                singleThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        retrying = true;
                        for (int i = 0; i < 20; i++) {
                            if (destroyed) {
                                retrying = false;
                                return;
                            }
                            Handler handler = mWebSocketThread.getHandler();
                            WebSocketClient websocket = mWebSocketThread.getSocket();
                            if (handler != null && websocket != null) {
                                if (mWebSocketThread.getConnectState() == 2) {
                                    break;
                                } else if (mWebSocketThread.getConnectState() == 1) {
                                    continue;
                                } else {
                                    handler.sendEmptyMessage(MessageType.CONNECT);
                                }
                            } else {
                                break;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "retry()", e);
                                if (destroyed = true) {
                                    retrying = false;
                                    return;
                                } else {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                        retrying = false;
                    }
                });
            }
        }
    }

    /**
     * 销毁资源，并停止重连
     */
    void destroy() {
        destroyed = true;
        if (singleThreadPool != null) {
            singleThreadPool.shutdownNow();
        }
        mWebSocketThread = null;
    }
}
