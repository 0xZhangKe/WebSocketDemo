package com.zhangke.websocket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zhangke.zlog.ZLog;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ClientHandshakeBuilder;
import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

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

    private WebSocketClient webSocket;

    private WebSocketHandler mHandler;

    private boolean quit;
    private SocketListener mSocketListener;

    /**
     * 0-未连接
     * 1-正在连接
     * 2-已连接
     */
    private int connectStatus = 0;

    public WebSocketThread(String connectUrl) {
        this.connectUrl = connectUrl;
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        mHandler = new WebSocketHandler();
        quit = false;
        Looper.loop();
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void setSocketListener(SocketListener socketListener) {
        this.mSocketListener = socketListener;
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
                    if (webSocket != null && msg.obj instanceof String) {
                        if (webSocket.isConnecting() && !webSocket.isClosed()) {
                            send((String) msg.obj);
                        } else {
                            if (connectStatus == 0) {
                                mHandler.sendEmptyMessage(MessageType.CONNECT);
                            }
                            mHandler.sendMessageDelayed(msg, 500);
                        }
                    }
                    break;
            }
        }

        private void connect() {
            if (connectStatus == 0) {
                connectStatus = 1;
                try {
                    if (webSocket == null) {
                        webSocket = new WebSocketClient(new URI(connectUrl), new Draft_6455()) {

                            @Override
                            public void onOpen(ServerHandshake handshakedata) {
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
                                mHandler.sendEmptyMessage(MessageType.DISCONNECT);
                                if (mSocketListener != null) {
                                    mSocketListener.onDisconnected();
                                }
                            }

                            @Override
                            public void onError(Exception ex) {
                                ZLog.e(TAG, "WebSocketClient#onError(Exception)", ex);
                            }
                        };
                        webSocket.connect();
                    } else {
                        webSocket.reconnect();
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
                if (webSocket != null) {
                    webSocket.close();
                }
                connectStatus = 0;
                ZLog.d(TAG, "WebSocket连接已关闭");
            }
        }

        /**
         * 结束此线程并销毁资源
         */
        private void quit() {
            disconnect();
            webSocket = null;
            Looper looper = Looper.myLooper();
            if (looper != null) {
                looper.quit();
            }
            quit = true;
            connectStatus = 0;
        }

        private void send(String text) {
            if (webSocket != null && webSocket.isConnecting() && !webSocket.isClosed()) {
                webSocket.send(text);
            }
        }

    }

}
