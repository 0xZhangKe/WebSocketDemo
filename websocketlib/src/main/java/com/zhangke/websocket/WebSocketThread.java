package com.zhangke.websocket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * WebSocket 线程，
 * 负责 WebSocket 连接的建立，数据发送，监听数据等。
 * <p>
 * Created by ZhangKe on 2018/6/11.
 */
public class WebSocketThread extends Thread {

    private static final String TAG = "WebSocketLib";

    /**
     * WebSocket 连接地址
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

    WebSocketThread(String connectUrl) {
        this.connectUrl = connectUrl;
        mReconnectManager = new ReconnectManager(this);
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        quit = false;
        mHandler = new WebSocketHandler();
        mHandler.sendEmptyMessage(MessageType.CONNECT);
        Looper.loop();
    }

    /**
     * 获取控制 WebSocketThread 的 Handler
     */
    public Handler getHandler() {
        return mHandler;
    }

    public WebSocketClient getSocket() {
        return mWebSocket;
    }

    public void setSocketListener(SocketListener socketListener) {
        this.mSocketListener = socketListener;
    }

    /**
     * 获取连接状态
     */
    public int getConnectState() {
        return connectStatus;
    }

    public void reconnect() {
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
                        mSocketListener.onMessageResponse(new TextResponse((String) msg.obj));
                    }
                    break;
                case MessageType.SEND_MESSAGE:
                    if (msg.obj instanceof String) {
                        String message = (String) msg.obj;
                        if (mWebSocket != null && connectStatus == 2) {
                            send(message);
                        } else if (mSocketListener != null) {
                            ErrorResponse errorResponse = new ErrorResponse();
                            errorResponse.setErrorCode(1);
                            errorResponse.setCause(new Throwable("WebSocket does not connect or closed!"));
                            errorResponse.setRequestText(message);
                            mSocketListener.onSendMessageError(errorResponse);
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
                        if(TextUtils.isEmpty(WebSocketSetting.getConnectUrl())){
                            throw new RuntimeException("WebSocket connect url is empty!");
                        }
                        mWebSocket = new WebSocketClient(new URI(connectUrl), new Draft_6455()) {

                            @Override
                            public void onOpen(ServerHandshake handShakeData) {
                                connectStatus = 2;
                                Log.d(TAG, "WebSocket 连接成功");
                                if (mSocketListener != null) {
                                    mSocketListener.onConnected();
                                }
                            }

                            @Override
                            public void onMessage(String message) {
                                connectStatus = 2;
                                Log.d(TAG, "WebSocket 接收到消息：" + message);
                                Message msg = mHandler.obtainMessage();
                                msg.what = MessageType.RECEIVE_MESSAGE;
                                msg.obj = message;
                                mHandler.sendMessage(msg);
                            }

                            @Override
                            public void onClose(int code, String reason, boolean remote) {
                                connectStatus = 0;
                                Log.d(TAG, "WebSocket 断开连接");
                                if (mSocketListener != null) {
                                    mSocketListener.onDisconnected();
                                }
                                mReconnectManager.performReconnect();
                            }

                            @Override
                            public void onError(Exception ex) {
                                Log.e(TAG, "WebSocketClient#onError(Exception)", ex);
                            }
                        };
                        Log.d(TAG, "WebSocket 开始连接...");
                        mWebSocket.connect();
                    } else {
                        Log.d(TAG, "WebSocket 开始重新连接...");
                        mWebSocket.reconnect();
                    }
                } catch (URISyntaxException e) {
                    connectStatus = 0;
                    Log.d(TAG, "WebSocket 连接失败");
                    if (mSocketListener != null) {
                        mSocketListener.onConnectError(e);
                    }
                }
            }
        }

        private void disconnect() {
            if (connectStatus == 2) {
                Log.d(TAG, "正在关闭WebSocket连接");
                if (mWebSocket != null) {
                    mWebSocket.close();
                }
                connectStatus = 0;
                Log.d(TAG, "WebSocket连接已关闭");
            }
        }

        private void send(String text) {
            if (mWebSocket != null && connectStatus == 2) {
                try {
                    mWebSocket.send(text);
                    Log.d(TAG, "数据发送成功：" + text);
                } catch (WebsocketNotConnectedException e) {
                    connectStatus = 0;
                    Log.e(TAG, "send()" + text, e);
                    Log.e(TAG, "连接已断开，数据发送失败：" + text, e);
                    if (mSocketListener != null) {
                        mSocketListener.onDisconnected();

                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.setErrorCode(1);
                        errorResponse.setCause(new Throwable("WebSocket does not connected or closed!"));
                        errorResponse.setRequestText(text);
                        mSocketListener.onSendMessageError(errorResponse);
                    }
                    mReconnectManager.performReconnect();
                }
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
