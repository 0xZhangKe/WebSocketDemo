package com.zhangke.websocket;

import android.text.TextUtils;

/**
 * 出现错误时的响应
 * Created by ZhangKe on 2018/6/25.
 */
public class ErrorResponse {

    /**
     * 1-WebSocket 未连接或已断开
     * 2-WebSocketService 服务未绑定到当前 Activity/Fragment，或绑定失败
     * 3-WebSocket 初始化未完成
     * 11-数据获取成功，但是解析 JSON 失败
     * 12-数据获取成功，但是服务器返回数据中的code值不正确
     */
    private int errorCode;
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
    /**
     * 错误描述，客户端可以通过这个字段来设置统一的错误提示等等
     */
    private String description;

    /**
     * 保留字段，可以自定义存放任意数据
     */
    private Object reserved;

    public ErrorResponse() {
    }

    /**
     * 1-WebSocket 未连接或已断开
     * 2-WebSocketService 服务未绑定到当前 Activity/Fragment，或绑定失败
     * 3-WebSocket 初始化未完成
     * 11-数据获取成功，但是解析 JSON 失败
     * 12-数据获取成功，但是服务器返回数据中的code值不正确
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 1-WebSocket 未连接或已断开
     * 2-WebSocketService 服务未绑定到当前 Activity/Fragment，或绑定失败
     * 3-WebSocket 初始化未完成
     * 11-数据获取成功，但是解析 JSON 失败
     * 12-数据获取成功，但是服务器返回数据中的code值不正确
     */
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getReserved() {
        return reserved;
    }

    public void setReserved(Object reserved) {
        this.reserved = reserved;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append("hashCode=");
        builder.append(hashCode());
        builder.append(",");
        builder.append("errorCode=");
        builder.append(errorCode);
        builder.append(",");
        builder.append("cause=");
        builder.append(cause.toString());
        builder.append(",");
        if (!TextUtils.isEmpty(requestText)) {
            builder.append("requestText=");
            builder.append(requestText);
            builder.append(",");
        }
        if (!TextUtils.isEmpty(responseText)) {
            builder.append("responseText=");
            builder.append(responseText);
            builder.append(",");
        }
        builder.append("description=");
        builder.append(description);
        builder.append(",");
        if (reserved != null) {
            builder.append("reserved=");
            builder.append(reserved.toString());
            builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }
}
