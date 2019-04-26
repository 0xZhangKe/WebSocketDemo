package com.zhangke.smaple.dispatersample;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.zhangke.websocket.WebSocketHandler;
import com.zhangke.websocket.WebSocketSetting;

/**
 * Created by ZhangKe on 2018/6/27.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        WebSocketSetting setting = new WebSocketSetting();
        setting.setConnectUrl("url");//必填
        setting.setReconnectWithNetworkChanged(true);
        setting.setProcessDataOnBackground(true);
        setting.setReconnectFrequency(20);
        setting.setResponseProcessDispatcher(new AppResponseDispatcher());
        setting.setConnectTimeout(60);

        WebSocketHandler.init(setting)
                .start();

        //具有监听网络变化权限，可选
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //注册网路连接状态变化广播
            WebSocketHandler.registerNetworkChangedReceiver(this);
        }
    }
}
