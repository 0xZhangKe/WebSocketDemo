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
    private boolean webSocketServiceBindSuccess;
    protected WebSocketService mWebSocketService;

    protected ServiceConnection mWebSocketServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webSocketServiceBindSuccess = true;
            webSocketPage.onServiceBindSuccess();
            mWebSocketService = ((WebSocketService.ServiceBinder) service).getService();
            mWebSocketService.addListener(mSocketListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketServiceBindSuccess = false;
            Log.e(TAG, "onServiceDisconnected:" + name);
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

    public void onCreate(){
        Intent intent = new Intent(context, WebSocketService.class);
        context.bindService(intent, mWebSocketServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void sendText(String text){
        if (webSocketServiceBindSuccess && mWebSocketService != null) {
            mWebSocketService.sendText(text);
        } else {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorCode(2);
            errorResponse.setCause(new Throwable("WebSocketService dose not bind!"));
            errorResponse.setRequestText(text);
            mSocketListener.onSendMessageError(errorResponse);
        }
    }

    public void reconnect(){
        if(mWebSocketService == null){
            mSocketListener.onConnectError(new Throwable("WebSocket dose not ready"));
        }else{
            mWebSocketService.reconnect();
        }
    }

    public void onDestroy(){
        context.unbindService(mWebSocketServiceConnection);
        webSocketServiceBindSuccess = false;
        mWebSocketService.removeListener(mSocketListener);
    }
}
