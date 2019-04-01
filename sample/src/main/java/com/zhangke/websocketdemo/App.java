package com.zhangke.websocketdemo;

import android.app.Application;
import android.content.Intent;

import com.zhangke.websocket.WebSocketHandler;
import com.zhangke.websocket.WebSocketSetting;

/**
 * Created by ZhangKe on 2018/6/27.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //注册网路连接状态变化广播
        WebSocketHandler.registerNetworkChangedReceiver(this);

        WebSocketSetting setting = new WebSocketSetting();
        setting.setConnectUrl("Your WebSocket connect url");//必选
        setting.setReconnectWithNetworkChanged(true);
        setting.setProcessDataOnBackground(true);
        setting.setReconnectFrequency(20);
        setting.setResponseProcessDispatcher(new AppResponseDispatcher());
        setting.setConnectTimeout(60);

        WebSocketHandler.initDefaultWebSocket(setting)
                .init();

    }
}
