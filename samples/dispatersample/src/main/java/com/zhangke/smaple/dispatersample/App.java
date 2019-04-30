package com.zhangke.smaple.dispatersample;

import android.app.Application;

import com.zhangke.websocket.WebSocketHandler;
import com.zhangke.websocket.WebSocketManager;
import com.zhangke.websocket.WebSocketSetting;

/**
 * Created by ZhangKe on 2018/6/27.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initWebSocket();
    }

    private void initWebSocket(){
        WebSocketSetting setting = new WebSocketSetting();
        //连接地址，必填，例如 wss://echo.websocket.org
        setting.setConnectUrl("your connect url");//必填

        //设置连接超时时间
        setting.setConnectTimeout(15 * 1000);

        //设置心跳间隔时间
        setting.setConnectionLostTimeout(60);

        //设置断开后的重连次数，可以设置的很大，不会有什么性能上的影响
        setting.setReconnectFrequency(60);

//        //设置Header
//        setting.setHttpHeaders(header);

        //设置消息分发器，接收到数据后先进入该类中处理，处理完再发送到下游
        setting.setResponseProcessDispatcher(new AppResponseDispatcher());
        //接收到数据后是否放入子线程处理，只有设置了 ResponseProcessDispatcher 才有意义
        setting.setProcessDataOnBackground(true);

        //网络状态发生变化后是否重连，
        //需要调用 WebSocketHandler.registerNetworkChangedReceiver(context) 方法注册网络监听广播
        setting.setReconnectWithNetworkChanged(true);

        //通过 init 方法初始化默认的 WebSocketManager 对象
        WebSocketManager manager = WebSocketHandler.init(setting);
        //启动连接
        manager.start();

        //注意，需要在 AndroidManifest 中配置网络状态获取权限
        //注册网路连接状态变化广播
        WebSocketHandler.registerNetworkChangedReceiver(this);
    }
}
