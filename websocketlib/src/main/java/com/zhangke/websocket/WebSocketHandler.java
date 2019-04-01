package com.zhangke.websocket;

import com.zhangke.websocket.dispatcher.ResponseProcessEngine;

/**
 * WebSocket控制
 * <p>
 * Created by ZhangKe on 2018/12/29.
 */
public class WebSocketHandler {

    /**
     * 消息发送引擎
     */
    private static WebSocketEngine webSocketEngine;
    /**
     * 消息处理引擎
     */
    private static ResponseProcessEngine responseProcessEngine;
    /**
     * 默认的 WebSocket 连接
     */
    private static WebSocketManager defaultWebSocket;

    /**
     * 初始化默认的 WebSocket 连接
     *
     * @param setting 该连接的相关设置参数
     */
    public static void initDefaultWebSocket(WebSocketSetting setting) {
        checkEngineNullAndInit();
        defaultWebSocket = new WebSocketManager(setting,
                webSocketEngine,
                responseProcessEngine);
    }

    /**
     * 获取默认的 WebSocket 连接，
     * 调用此方法之前需要先调用 {@link #initDefaultWebSocket(WebSocketSetting)} 方法初始化
     *
     * @return 返回一个 {@link WebSocketManager} 实例
     */
    public static WebSocketManager getDefault() {
        return defaultWebSocket;
    }

    private static void checkEngineNullAndInit() {
        if (webSocketEngine == null) {
            synchronized (WebSocketHandler.class) {
                webSocketEngine = new WebSocketEngine();
            }
        }
        if (responseProcessEngine == null) {
            synchronized (WebSocketHandler.class) {
                responseProcessEngine = new ResponseProcessEngine();
            }
        }
    }

}
