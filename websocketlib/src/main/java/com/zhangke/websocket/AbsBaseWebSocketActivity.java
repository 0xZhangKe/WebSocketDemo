package com.zhangke.websocket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.zhangke.zlog.ZLog;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by ZhangKe on 2018/6/25.
 */
public abstract class AbsBaseWebSocketActivity<T extends BaseWebSocketService> extends AppCompatActivity {

    private static final String TAG = "AbsBaseWebSocketActivity";

    /**
     * WebSocket 服务是否绑定成功
     */
    private boolean webSocketServiceBindSuccess = false;
    protected BaseWebSocketService mWebSocketService;

    protected ServiceConnection mWebSocketServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webSocketServiceBindSuccess = true;
            onServiceBindSuccess();
            mWebSocketService = ((BaseWebSocketService.ServiceBinder) service).getService();
            mWebSocketService.addListener(mWebSocketService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketServiceBindSuccess = false;
            ZLog.e(TAG, "onServiceDisconnected:" + name);
        }
    };

    private SocketListener mSocketListener = new SocketListener() {
        @Override
        public void onConnected() {
            AbsBaseWebSocketActivity.this.onConnected();
        }

        @Override
        public void onConnectError(Throwable cause) {
            AbsBaseWebSocketActivity.this.onConnectError(cause);
        }

        @Override
        public void onDisconnected() {
            AbsBaseWebSocketActivity.this.onDisconnected();
        }

        @Override
        public void onMessageResponse(String message) {
            AbsBaseWebSocketActivity.this.onMessageResponse(message);
        }

        @Override
        public void onSendMessageError(String error) {
            AbsBaseWebSocketActivity.this.onSendMessageError(error);
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
            mSocketListener.onSendMessageError(text);
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

    protected abstract void onMessageResponse(String message);

    protected abstract void onSendMessageError(String error);

    /**
     * 绑定服务
     */
    private void bindWebSocketService() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        Intent intent = new Intent(this, (Class<?>) actualTypeArguments[0]);
        bindService(intent, mWebSocketServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(mWebSocketServiceConnection);
        webSocketServiceBindSuccess = false;
        mWebSocketService.removeListener(mSocketListener);
        super.onDestroy();
    }
}
