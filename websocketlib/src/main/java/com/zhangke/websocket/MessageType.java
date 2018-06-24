package com.zhangke.websocket;

/**
 * 消息类型
 * Created by ZhangKe on 2018/6/11.
 */
public class MessageType {

    public static final int CONNECT = 0;//连接Socket
    public static final int DISCONNECT = 1;//断开连接，主动关闭或被动关闭
    public static final int QUIT = 2;//结束线程
    public static final int SEND_MESSAGE = 3;//通过Socket连接发送数据
    public static final int RECEIVE_MESSAGE = 4;//通过Socket获取到数据

}
