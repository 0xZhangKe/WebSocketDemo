package com.zhangke.websocket;

/**
 * WebSocket控制
 * <p>
 * Created by ZhangKe on 2018/12/29.
 */
public class WebSocketHandler {

    private static volatile WebSocketHandler mInstance;

    public WebSocketHandler getInstance() {
        if (mInstance == null) {
            synchronized (WebSocketHandler.class) {
                mInstance = new WebSocketHandler();
            }
        }
        return mInstance;
    }

    private WebSocketHandler() {
    }

    private WebSocketSetting mSetting;
    private WebSocketWrapper mWebSocketThread;

    public void init(WebSocketSetting setting){
        this.mSetting = setting;
        mWebSocketThread = new WebSocketWrapper(setting);
    }



}
