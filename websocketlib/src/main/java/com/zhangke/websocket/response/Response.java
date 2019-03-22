package com.zhangke.websocket.response;

/**
 * WebSocket 响应数据接口
 * Created by ZhangKe on 2018/6/26.
 */
public interface Response<T> {

    /**
     * 获取响应的数据
     */
    T getResponseData();

    /**
     * 设置响应的数据
     */
    void setResponseData(T responseData);
}
