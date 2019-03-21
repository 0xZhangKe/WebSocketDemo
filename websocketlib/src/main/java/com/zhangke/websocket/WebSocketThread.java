package com.zhangke.websocket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.MessageType;
import com.zhangke.websocket.response.TextResponse;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * WebSocket 线程，
 * 负责 WebSocket 连接的建立，数据发送，监听数据等。
 * <p>
 * Created by ZhangKe on 2018/6/11.
 */
public class WebSocketThread extends Thread {

    private static final String TAG = "WebSocketLib";

    private WebSocketSetting mSetting;

    private WebSocketClient mWebSocket;

    private WebSocketHandler mHandler;

    private SocketListener mSocketListener;

    /**
     * 0-未连接
     * 1-正在连接
     * 2-已连接
     */
    private int connectStatus = 0;

    WebSocketThread(WebSocketSetting setting, SocketListener socketListener) {
        this.mSetting = setting;
        this.mSocketListener = socketListener;
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        mHandler = new WebSocketHandler();
        mHandler.sendEmptyMessage(MessageType.CONNECT);
        Looper.loop();
    }

    public void connect() {
        if (getConnectState() == 0) {
            setConnectState(1);
            try {
                if (mWebSocket == null) {
                    if (TextUtils.isEmpty(mSetting.getConnectUrl())) {
                        throw new RuntimeException("WebSocket connect url is empty!");
                    }
                    mWebSocket = new WebSocketClient(new URI(mSetting.getConnectUrl()), new Draft_6455()) {

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
                        }

                        @Override
                        public void onError(Exception ex) {
                            Log.e(TAG, "WebSocketClient#onError(Exception)", ex);
                        }
                    };
                    Log.d(TAG, "WebSocket 开始连接...");
                    if (mSetting.getProxy() != null) {
                        mWebSocket.setProxy(mSetting.getProxy());
                    }
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

    /**
     * 断开连接
     */
    public void disConnect() {
        if (connectStatus == 2) {
            Log.d(TAG, "正在关闭WebSocket连接");
            if (mWebSocket != null) {
                mWebSocket.close();
            }
            connectStatus = 0;
            Log.d(TAG, "WebSocket连接已关闭");
        }
    }

    /**
     * 发送数据
     */
    public void send(String text) {
        if (connectStatus == 2) {
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
            }
        }
        doSendData(text);
    }

    /**
     * 发送数据
     */
    public void send(byte[] bytes) {
        doSendData(bytes);
    }

    /**
     * 发送数据
     */
    public void send(ByteBuffer byteBuffer) {
        doSendData(byteBuffer);
    }

    private void doSendData(Object object) {
        String errorDescription = "";
        if (connectStatus == 2) {
            try {
                Class clazz = object.getClass();
                if (clazz == String.class) {
                    errorDescription = (String) object;
                    mWebSocket.send(errorDescription);
                } else if (clazz == byte[].class) {
                    mWebSocket.send((byte[]) object);
                    errorDescription = "byte[]";
                } else if (clazz == ByteBuffer.class) {
                    mWebSocket.send((ByteBuffer) object);
                    errorDescription = "ByteBuffer";
                } else {
                    //ignore
                    errorDescription = "other";
                }
                LogUtil.i(TAG, "数据发送成功：" + errorDescription);
            } catch (WebsocketNotConnectedException e) {
                connectStatus = 0;
                LogUtil.e(TAG, "连接已断开，数据发送失败：" + errorDescription, e);
            } catch (Throwable e) {
                connectStatus = 0;
                LogUtil.e(TAG, "连接已断开，数据发送失败：" + errorDescription, e);
            }
        } else if(connectStatus == 0 || connectStatus == 1){
            LogUtil.e(TAG, "WebSocket 未连接，数据发送失败：" + errorDescription);

        }
    }

    /**
     * 获取连接状态
     */
    int getConnectState() {
        return connectStatus;
    }

    /**
     * 设置连接状态
     */
    private void setConnectState(int state) {
        this.connectStatus = state;
    }

    void reconnect() {

    }


    private static class WebSocketHandler extends Handler {

        private WebSocketThread thread;
        private SocketListener listener;

        WebSocketHandler(WebSocketThread thread, SocketListener listener) {
            this.thread = thread;
            this.listener = listener;
        }

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
                    if (listener != null && msg.obj instanceof String) {
                        listener.onMessageResponse(new TextResponse((String) msg.obj));
                    }
                    break;
                case MessageType.SEND_MESSAGE:
                    if (msg.obj instanceof String) {
                        String message = (String) msg.obj;
                        if (listener != null && thread.getConnectState() == 2) {
                            send(message);
                        } else if (listener != null) {
                            ErrorResponse errorResponse = new ErrorResponse();
                            errorResponse.setErrorCode(1);
                            errorResponse.setCause(new Throwable("WebSocket does not connect or closed!"));
                            errorResponse.setRequestText(message);
                            listener.onSendMessageError(errorResponse);
                        }
                    }
                    break;
            }
        }

        private void connect() {
        }

        private void disconnect() {
        }

        private void send(String text) {
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
            thread.interrupt();
        }
    }
}
