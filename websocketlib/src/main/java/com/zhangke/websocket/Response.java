package com.zhangke.websocket;

/**
 * WebSocket 响应数据
 * Created by ZhangKe on 2018/6/26.
 */
public class Response<T> {

    private String responseText;
    private T responseEntity;

    public Response(String responseText, T responseEntity) {
        this.responseText = responseText;
        this.responseEntity = responseEntity;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public T getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(T responseEntity) {
        this.responseEntity = responseEntity;
    }
}
