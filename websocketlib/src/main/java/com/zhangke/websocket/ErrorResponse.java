package com.zhangke.websocket;

/**
 * 出现错误时的响应
 * Created by ZhangKe on 2018/6/25.
 */
public class ErrorResponse {

    /**
     * 1-WebSocket 未连接或已断开
     * 2-WebSocketService 服务未绑定到当前 Activity/Fragment，或绑定失败
     * 10-WebSocket 初始化未完成
     */
    private int errorCode = 0;
    /**
     * 错误原因
     */
    private Throwable cause;
    /**
     * 发送的数据，可能为空
     */
    private String requestText;
    /**
     * 响应的数据，可能为空
     */
    private String responseText;

    public ErrorResponse() {
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public String getRequestText() {
        return requestText;
    }

    public void setRequestText(String requestText) {
        this.requestText = requestText;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
}
