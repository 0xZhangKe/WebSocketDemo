package com.zhangke.websocket;

/**
 * Created by ZhangKe on 2018/6/26.
 */
public class WebSocketSetting {

    private static String connectUrl;
    private static Class genericType;
    private static IResponseDispatcher responseProcessDelivery = new CommonResponseDispacther();

    /**
     * 获取 WebSocket 链接地址
     */
    public static String getConnectUrl() {
        return connectUrl;
    }

    /**
     * 设置 WebSocket 链接地址，第一次设置有效，
     * 必须在启动 WebSocket 线程之前设置
     */
    public static void setConnectUrl(String connectUrl) {
        WebSocketSetting.connectUrl = connectUrl;
    }

    /**
     * 获取响应数据转换为对象的泛型类型
     */
    public static Class getGenericType() {
        return genericType;
    }

    /**
     * 设置响应数据转换为对象的泛型类型
     */
    public static void setGenericType(Class genericType) {
        WebSocketSetting.genericType = genericType;
    }

    public static IResponseDispatcher getResponseProcessDelivery() {
        return responseProcessDelivery;
    }

    public static void setResponseProcessDelivery(IResponseDispatcher responseProcessDelivery) {
        WebSocketSetting.responseProcessDelivery = responseProcessDelivery;
    }
}
