package com.zhangke.websocket;

/**
 * WebSocket 响应数据
 * Created by ZhangKe on 2018/6/26.
 */
public interface Response<T> {

    String getResponseText();

    void setResponseText(String responseText);

    T getResponseEntity();

    void setResponseEntity(T responseEntity);
}
