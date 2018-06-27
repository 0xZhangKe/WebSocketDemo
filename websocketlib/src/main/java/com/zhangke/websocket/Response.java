package com.zhangke.websocket;

/**
 * WebSocket 响应数据接口
 * Created by ZhangKe on 2018/6/26.
 */
public interface Response<T> {

    /**
     * 获取响应的文本数据
     */
    String getResponseText();

    /**
     * 设置响应的文本数据
     */
    void setResponseText(String responseText);

    /**
     * 获取该数据的实体，可能为空，具体看实现类
     */
    T getResponseEntity();

    /**
     * 设置数据实体
     */
    void setResponseEntity(T responseEntity);
}
