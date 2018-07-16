package com.zhangke.websocket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

/**
 * 负责页面的 WebSocketService 绑定等操作
 * Created by ZhangKe on 2018/6/28.
 */
public class WebSocketServiceConnectManager {

    private static final String TAG = "WebSocketLib";

    private Context context;
    private IWebSocketPage webSocketPage;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * WebSocket 服务是否绑定成功
     */
    private boolean webSocketServiceBindSuccess = false;
    protected WebSocketService mWebSocketService;

    private int bindTime = 0;
    /**
     * 是否正在绑定服务
     */
    private boolean binding = false;

    protected ServiceConnection mWebSocketServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webSocketServiceBindSuccess = true;
            binding = false;
            bindTime = 0;
            mWebSocketService = ((WebSocketService.ServiceBinder) service).getService();
            mWebSocketService.addListener(mSocketListener);
            webSocketPage.onServiceBindSuccess();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binding = false;
            webSocketServiceBindSuccess = false;
            Log.e(TAG, "onServiceDisconnected:" + name);
            if (bindTime < 5 && !binding) {
                Log.d(TAG, String.format("WebSocketService 连接断开，开始第%s次重连", bindTime));
                bindService();
            }
        }
    };

    private SocketListener mSocketListener = new SocketListener() {
        @Override
        public void onConnected() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    webSocketPage.onConnected();
                }
            });
        }

        @Override
        public void onConnectError(final Throwable cause) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    webSocketPage.onConnectError(cause);
                }
            });
        }

        @Override
        public void onDisconnected() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    webSocketPage.onDisconnected();
                }
            });
        }

        @Override
        public void onMessageResponse(final Response message) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    webSocketPage.onMessageResponse(message);
                }
            });
        }

        @Override
        public void onSendMessageError(final ErrorResponse error) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    webSocketPage.onSendMessageError(error);
                }
            });
        }
    };

    public WebSocketServiceConnectManager(Context context, IWebSocketPage webSocketPage) {
        this.context = context;
        this.webSocketPage = webSocketPage;
        webSocketServiceBindSuccess = false;
    }

    public void onCreate() {
        bindService();
    }

    private void bindService() {
        binding = true;
        webSocketServiceBindSuccess = false;
        Intent intent = new Intent(context, WebSocketService.class);
        context.bindService(intent, mWebSocketServiceConnection, Context.BIND_AUTO_CREATE);
        bindTime++;
    }

    public void sendText(String text) {
        if (webSocketServiceBindSuccess && mWebSocketService != null) {
            mWebSocketService.sendText(text);
        } else {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorCode(2);
            errorResponse.setCause(new Throwable("WebSocketService dose not bind!"));
            errorResponse.setRequestText(text);
            ResponseDelivery delivery = new ResponseDelivery();
            delivery.addListener(mSocketListener);
            WebSocketSetting.getResponseProcessDelivery().onSendMessageError(errorResponse, delivery);
            if (!binding) {
                bindTime = 0;
                Log.d(TAG, String.format("WebSocketService 连接断开，开始第%s次重连", bindTime));
                bindService();
            }
        }
    }

    public void reconnect() {
        if (webSocketServiceBindSuccess && mWebSocketService != null) {
            mWebSocketService.reconnect();
        } else {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorCode(2);
            errorResponse.setCause(new Throwable("WebSocketService dose not bind!"));
            ResponseDelivery delivery = new ResponseDelivery();
            delivery.addListener(mSocketListener);
            WebSocketSetting.getResponseProcessDelivery().onSendMessageError(errorResponse, delivery);
            if (!binding) {
                bindTime = 0;
                Log.d(TAG, String.format("WebSocketService 连接断开，开始第%s次重连", bindTime));
                bindService();
            }
        }
    }

    public void onDestroy() {
        binding = false;
        bindTime = 0;
        context.unbindService(mWebSocketServiceConnection);
        Log.d(TAG, context.toString() + "已解除 WebSocketService 绑定");
        webSocketServiceBindSuccess = false;
        mWebSocketService.removeListener(mSocketListener);
    }
}
