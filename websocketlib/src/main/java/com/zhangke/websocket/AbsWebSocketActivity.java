package com.zhangke.websocket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by ZhangKe on 2018/6/25.
 */
public abstract class AbsWebSocketActivity extends AppCompatActivity {

    private static final String TAG = "AbsWebSocketActivity";

    /**
     * WebSocket 服务是否绑定成功
     */
    private boolean webSocketServiceBindSuccess = false;
    protected WebSocketService mWebSocketService;

    protected ServiceConnection mWebSocketServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webSocketServiceBindSuccess = true;
            onServiceBindSuccess();
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AbsWebSocketActivity.this.onConnected();
                }
            });
        }

        @Override
        public void onConnectError(final Throwable cause) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AbsWebSocketActivity.this.onConnectError(cause);
                }
            });
        }

        @Override
        public void onDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AbsWebSocketActivity.this.onDisconnected();
                }
            });
        }

        @Override
        public void onMessageResponse(final Response message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AbsWebSocketActivity.this.onMessageResponse(message);
                }
            });
        }

        @Override
        public void onSendMessageError(final ErrorResponse error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AbsWebSocketActivity.this.onSendMessageError(error);
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webSocketServiceBindSuccess = false;
        bindWebSocketService();
    }

    protected void sendText(String text) {
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

    /**
     * 服务绑定成功时的回调
     */
    protected void onServiceBindSuccess() {

    }

    protected void onConnected() {

    }

    protected void onConnectError(Throwable cause) {

    }

    protected void onDisconnected() {

    }

    protected abstract void onMessageResponse(Response message);

    protected abstract void onSendMessageError(ErrorResponse error);

    /**
     * 绑定服务
     */
    private void bindWebSocketService() {
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mWebSocketServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            unbindService(mWebSocketServiceConnection);
            webSocketServiceBindSuccess = false;
            mWebSocketService.removeListener(mSocketListener);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
