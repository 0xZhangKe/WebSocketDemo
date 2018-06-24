package com.zhangke.websocketdemo;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zhangke.websocket.BaseWebSocketService;

/**
 * Created by zk721 on 2018/1/28.
 */

public class WebSocketService extends BaseWebSocketService {

    @Override
    protected String getConnectUrl() {
        return "服务器对应的url";
    }

    private WebSocketService.ServiceBinder serviceBinder = new WebSocketService.ServiceBinder();

    public class ServiceBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (serviceBinder == null) {
            serviceBinder = new WebSocketService.ServiceBinder();
        }
        return serviceBinder;
    }

}
