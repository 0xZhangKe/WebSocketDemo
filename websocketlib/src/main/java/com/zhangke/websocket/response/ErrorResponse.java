package com.zhangke.websocket.response;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zhangke.websocket.request.ByteArrayRequest;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 出现错误时的响应
 * Created by ZhangKe on 2018/6/25.
 */
public class ErrorResponse {

    private static Queue<ErrorResponse> CACHE_QUEUE = new ArrayDeque<>(10);

    public static ErrorResponse obtain() {
        ErrorResponse request = CACHE_QUEUE.poll();
        if (request == null) {
            request = new ErrorResponse();
        }
        return request;
    }

    public static void release(ErrorResponse request) {
        CACHE_QUEUE.offer(request);
    }

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
    private Object requestData;
    /**
     * 响应的数据，可能为空
     */
    private Object responseData;
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

    public Object getRequestData() {
        return requestData;
    }

    public void setRequestData(Object requestData) {
        this.requestData = requestData;
    }

    public Object getResponseData() {
        return responseData;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
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

    @NonNull
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
        builder.append("requestData=");
        String request;
        if (requestData != null) {
            if (requestData instanceof String) {
                request = (String) requestData;
            } else {
                request = requestData.toString();
            }
        } else {
            request = "null";
        }
        builder.append(request);
        builder.append(",");
        builder.append("responseData=");
        String response;
        if (responseData != null) {
            if (responseData instanceof String) {
                response = (String) responseData;
            } else {
                response = responseData.toString();
            }
        } else {
            response = "null";
        }
        builder.append(response);
        builder.append(",");
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
