package com.zhangke.websocket.dispatcher;

import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static com.zhangke.websocket.util.ThreadUtil.checkMainThread;
import static com.zhangke.websocket.util.ThreadUtil.runOnMainThread;

/**
 * 消息发射器接口
 *
 * Created by ZhangKe on 2018/6/26.
 */
public interface ResponseDelivery extends SocketListener {

    void addListener(SocketListener listener);

    void removeListener(SocketListener listener);

    boolean isEmpty();
}
