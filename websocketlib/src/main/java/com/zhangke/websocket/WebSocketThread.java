package com.zhangke.websocket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zhangke.zlog.ZLog;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * WebSocket线程
 * <p>
 * Created by ZhangKe on 2018/6/11.
 */
public class WebSocketThread extends Thread {

    private static final String TAG = "WebSocketLib";

    /**
     * WebSocket链接地址
     */
    private String connectUrl;

    private WebSocketClient mWebSocket;

    private WebSocketHandler mHandler;

    private boolean quit;
    private SocketListener mSocketListener;

    private ReconnectManager mReconnectManager;

    /**
     * 0-未连接
     * 1-正在连接
     * 2-已连接
     */
    private int connectStatus = 0;

    public WebSocketThread(String connectUrl) {
        this.connectUrl = connectUrl;
        mReconnectManager = new ReconnectManager(this);
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        mHandler = new WebSocketHandler();
        quit = false;
        Looper.loop();
    }

    Handler getHandler() {
        return mHandler;
    }

    WebSocketClient getSocket() {
        return mWebSocket;
    }

    void setSocketListener(SocketListener socketListener) {
        this.mSocketListener = socketListener;
    }

    /**
     * 获取连接状态
     */
    int getConnectState() {
        return connectStatus;
    }

    void reconnect(){
        mReconnectManager.performReconnect();
    }

    private class WebSocketHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MessageType.CONNECT:
                    connect();
                    break;
                case MessageType.DISCONNECT:
                    disconnect();
                    break;
                case MessageType.QUIT:
                    quit();
                    break;
                case MessageType.RECEIVE_MESSAGE:
                    if (mSocketListener != null && msg.obj instanceof String) {
                        mSocketListener.onTextMessage((String) msg.obj);
                    }
                    break;
                case MessageType.SEND_MESSAGE:
                    if (mWebSocket != null && msg.obj instanceof String) {
                        if (mWebSocket.isConnecting() && !mWebSocket.isClosed()) {
                            send((String) msg.obj);
                        } else if (mSocketListener != null) {
                            mSocketListener.onTextMessage((String) msg.obj);
                            mReconnectManager.performReconnect();
                        }
                    }
                    break;
            }
        }

        private void connect() {
            if (connectStatus == 0) {
                connectStatus = 1;
                try {
                    if (mWebSocket == null) {
                        mWebSocket = new WebSocketClient(new URI(connectUrl), new Draft_6455()) {

                            @Override
                            public void onOpen(ServerHandshake handShakeData) {
                                connectStatus = 2;
                                if (mSocketListener != null) {
                                    mSocketListener.onConnected();
                                }
                            }

                            @Override
                            public void onMessage(String message) {
                                connectStatus = 2;
                                Message msg = mHandler.obtainMessage();
                                msg.what = MessageType.RECEIVE_MESSAGE;
                                msg.obj = message;
                                mHandler.sendMessage(msg);
                            }

                            @Override
                            public void onClose(int code, String reason, boolean remote) {
                                connectStatus = 0;
                                mReconnectManager.performReconnect();
                                if (mSocketListener != null) {
                                    mSocketListener.onDisconnected();
                                }
                            }

                            @Override
                            public void onError(Exception ex) {
                                ZLog.e(TAG, "WebSocketClient#onError(Exception)", ex);
                            }
                        };
                        mWebSocket.connect();
                    } else {
                        mWebSocket.reconnect();
                    }
                } catch (URISyntaxException e) {
                    connectStatus = 0;
                    ZLog.e(TAG, "WebSocketThread$Handler#connect()", e);
                }
            }
        }

        private void disconnect() {
            if (connectStatus == 2) {
                ZLog.d(TAG, "正在关闭WebSocket连接");
                if (mWebSocket != null) {
                    mWebSocket.close();
                }
                connectStatus = 0;
                ZLog.d(TAG, "WebSocket连接已关闭");
            }
        }

        private void send(String text) {
            if (mWebSocket != null && mWebSocket.isConnecting() && !mWebSocket.isClosed()) {
                mWebSocket.send(text);
            }
        }

        /**
         * 结束此线程并销毁资源
         */
        private void quit() {
            disconnect();
            mWebSocket = null;
            mReconnectManager.destroy();
            quit = true;
            connectStatus = 0;
            Looper looper = Looper.myLooper();
            if (looper != null) {
                looper.quit();
            }
            WebSocketThread.this.interrupt();
        }
    }
}
