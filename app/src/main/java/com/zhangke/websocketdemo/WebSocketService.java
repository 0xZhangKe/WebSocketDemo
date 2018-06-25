package com.zhangke.websocketdemo;

import com.zhangke.websocket.BaseWebSocketService;
import com.zhangke.websocket.SocketListener;

/**
 * Created by zk721 on 2018/1/28.
 */

public class WebSocketService extends BaseWebSocketService implements SocketListener{

    @Override
    protected String getConnectUrl() {
        return "服务器对应的url";
    }


}
