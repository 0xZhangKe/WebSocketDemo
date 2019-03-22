package com.zhangke.websocket;

import android.text.TextUtils;

import com.zhangke.websocket.request.Request;
import com.zhangke.websocket.response.ByteBufferResponse;
import com.zhangke.websocket.response.TextResponse;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * 负责 WebSocket 连接的建立，数据发送，监听数据等。
 * <p>
 * Created by ZhangKe on 2018/6/11.
 */
public class WebSocketWrapper {

    private static final String TAG = "WebSocketLib";

    private WebSocketSetting mSetting;
    private SocketWrapperListener mSocketListener;

    private WebSocketClient mWebSocket;

    /**
     * 0-未连接
     * 1-正在连接
     * 2-已连接
     */
    private int connectStatus = 0;

    /**
     * 需要关闭连接标志，调用 #disconnect 方法后为 true
     */
    private boolean needClose = false;
    /**
     * 是否已销毁
     */
    private boolean destroyed = false;

    WebSocketWrapper(WebSocketSetting setting, SocketWrapperListener socketListener) {
        this.mSetting = setting;
        this.mSocketListener = socketListener;
    }

    void connect() {
        if (destroyed) {
            return;
        }
        needClose = false;
        if (connectStatus == 0) {
            connectStatus = 1;
            try {
                if (mWebSocket == null) {
                    if (TextUtils.isEmpty(mSetting.getConnectUrl())) {
                        throw new RuntimeException("WebSocket connect url is empty!");
                    }
                    mWebSocket = new WebSocketClient(new URI(mSetting.getConnectUrl()), new Draft_6455()) {

                        @Override
                        public void onOpen(ServerHandshake handShakeData) {
                            if (destroyed) {
                                checkDestroy();
                                return;
                            }
                            connectStatus = 2;
                            LogUtil.i(TAG, "WebSocket connect success");
                            if (needClose) {
                                disConnect();
                            } else {
                                if (mSocketListener != null) {
                                    mSocketListener.onConnected();
                                }
                            }
                        }

                        @Override
                        public void onMessage(String message) {
                            if (destroyed) {
                                checkDestroy();
                                return;
                            }
                            connectStatus = 2;
                            LogUtil.i(TAG, "WebSocket received string message：" + message);
                            if (mSocketListener != null) {
                                TextResponse textResponse = TextResponse.obtain();
                                textResponse.setResponseData(message);
                                mSocketListener.onReceivedData(textResponse);
                            }
                        }

                        @Override
                        public void onMessage(ByteBuffer bytes) {
                            if (destroyed) {
                                checkDestroy();
                                return;
                            }
                            connectStatus = 2;
                            LogUtil.i(TAG, "WebSocket received ByteBuffer message：" + bytes.array().length);
                            if (mSocketListener != null) {
                                ByteBufferResponse byteBufferResponse = ByteBufferResponse.obtain();
                                byteBufferResponse.setResponseData(bytes);
                                mSocketListener.onReceivedData(byteBufferResponse);
                            }
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {
                            connectStatus = 0;
                            LogUtil.d(TAG, "WebSocket disconnected");
                            if (mSocketListener != null) {
                                mSocketListener.onDisconnect();
                            }
                            checkDestroy();
                        }

                        @Override
                        public void onError(Exception ex) {
                            if (destroyed) {
                                checkDestroy();
                                return;
                            }
                            LogUtil.e(TAG, "WebSocketClient#onError(Exception)", ex);
                        }
                    };

                    LogUtil.i(TAG, "WebSocket start connect...");
                    if (mSetting.getProxy() != null) {
                        mWebSocket.setProxy(mSetting.getProxy());
                    }
                    mWebSocket.connect();
                    if (needClose) {
                        disConnect();
                    }
                    checkDestroy();
                } else {
                    LogUtil.i(TAG, "WebSocket reconnecting...");
                    mWebSocket.reconnect();
                    if (needClose) {
                        disConnect();
                    }
                    checkDestroy();
                }
            } catch (Throwable e) {
                connectStatus = 0;
                LogUtil.e(TAG, "WebSocket connect failed:", e);
                if (mSocketListener != null) {
                    mSocketListener.onConnectFailed(e);
                }
            }
        }
    }

    private void checkDestroy() {
        if (destroyed) {
            try {
                if (mWebSocket != null && !mWebSocket.isClosed()) {
                    mWebSocket.close();
                }
                releaseResource();
                connectStatus = 0;
            } catch (Throwable e) {
                LogUtil.e(TAG, "checkDestroy(WebSocketClient)", e);
            }
        }
    }

    /**
     * 重新连接
     */
    void reconnect() {
        needClose = false;
        if (connectStatus == 0) {
            connect();
        }
    }

    /**
     * 断开连接
     */
    void disConnect() {
        needClose = true;
        if (connectStatus == 2) {
            LogUtil.i(TAG, "WebSocket disconnecting...");
            if (mWebSocket != null) {
                mWebSocket.close();
            }
            LogUtil.i(TAG, "WebSocket disconnected");
        }
    }

    /**
     * 发送数据
     *
     * @param request 请求数据
     */
    void send(Request request) {
        if (mWebSocket == null) {
            return;
        }
        if (request == null) {
            LogUtil.e(TAG, "send data is null!");
            return;
        }
        if (connectStatus == 2) {
            try {
                request.send(mWebSocket);
                LogUtil.i(TAG, "send success:" + request.toString());
            } catch (WebsocketNotConnectedException e) {
                connectStatus = 0;
                LogUtil.e(TAG, "ws is disconnected, send failed:" + request.toString(), e);
                if (mSocketListener != null) {
                    mSocketListener.onSendDataError(request, 0, e);
                    mSocketListener.onDisconnect();
                }
            } catch (Throwable e) {
                connectStatus = 0;
                LogUtil.e(TAG, "Exception,send failed:" + request.toString(), e);
                if (mSocketListener != null) {
                    mSocketListener.onSendDataError(request, 1, e);
                }
            }
        } else {
            LogUtil.e(TAG, "WebSocket not connect,send failed:" + request.toString());
            if (mSocketListener != null) {
                mSocketListener.onSendDataError(request, 0, null);
            }
        }
    }

    /**
     * 获取连接状态
     */
    int getConnectState() {
        return connectStatus;
    }

    /**
     * 彻底销毁资源
     */
    void destroy() {
        destroyed = true;
        disConnect();
        if (connectStatus == 0) {
            mWebSocket = null;
        }
        releaseResource();
    }

    private void releaseResource() {
        if (mSocketListener != null) {
            mSocketListener = null;
        }
    }

}
