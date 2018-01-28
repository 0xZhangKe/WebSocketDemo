package com.zhangke.WebSocket.event;

/**
 * WebSocket 连接失败事件
 *
 * Created by ZhangKe on 2017/11/28.
 */

public class WebSocketConnectionErrorEvent {
    private String cause;//原因

    public WebSocketConnectionErrorEvent(String cause) {
        this.cause = cause;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
