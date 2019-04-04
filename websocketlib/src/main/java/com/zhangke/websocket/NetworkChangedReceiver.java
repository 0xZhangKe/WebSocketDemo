package com.zhangke.websocket;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.RequiresPermission;

import com.zhangke.websocket.util.LogUtil;

import java.util.Map;

/**
 * 监听网络变化广播，网络变化时自动重连
 * Created by ZhangKe on 2018/7/2.
 */
public class NetworkChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "WSNetworkReceiver";

    public NetworkChangedReceiver() {
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) return;
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        LogUtil.i(TAG, "网络连接发生变化，当前WiFi连接可用，正在尝试重连。");
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        LogUtil.i(TAG, "网络连接发生变化，当前移动连接可用，正在尝试重连。");
                    }
                    if (WebSocketHandler.getDefault() != null) {
                        if (WebSocketHandler.getDefault().getSetting().reconnectWithNetworkChanged()) {
                            WebSocketHandler.getDefault().reconnect();
                        }
                    }
                    if (!WebSocketHandler.getAllWebSocket().isEmpty()) {
                        Map<String, WebSocketManager> webSocketManagerMap = WebSocketHandler.getAllWebSocket();
                        for (String key : webSocketManagerMap.keySet()) {
                            WebSocketManager item = webSocketManagerMap.get(key);
                            if (item != null && item.getSetting().reconnectWithNetworkChanged()) {
                                item.reconnect();
                            }
                        }
                    }
                } else {
                    LogUtil.i(TAG, "当前没有可用网络");
                }
            }
        }
    }
}
