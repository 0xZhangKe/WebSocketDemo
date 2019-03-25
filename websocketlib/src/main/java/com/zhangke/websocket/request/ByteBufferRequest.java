package com.zhangke.websocket.request;

import android.support.annotation.NonNull;

import org.java_websocket.client.WebSocketClient;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * ByteBuffer 类型的请求
 * <p>
 * Created by ZhangKe on 2019/3/22.
 */
public class ByteBufferRequest implements Request<ByteBuffer> {

    private static Queue<ByteBufferRequest> CACHE_QUEUE = new ArrayDeque<>(10);

    public static ByteBufferRequest obtain() {
        ByteBufferRequest request = CACHE_QUEUE.poll();
        if (request == null) {
            request = new ByteBufferRequest();
        }
        return request;
    }

    public static void release(ByteBufferRequest request) {
        CACHE_QUEUE.offer(request);
    }

    private ByteBuffer data;

    @Override
    public void setRequestData(ByteBuffer data) {
        this.data = data;
    }

    @Override
    public ByteBuffer getRequestData() {
        return this.data;
    }

    @Override
    public void send(WebSocketClient client) {
        client.send(this.data);
    }

    @Override
    public void release() {
        release(this);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("[@ByteBufferRequest%s,ByteBuffer:%s]",
                hashCode(),
                data == null ?
                        "null" :
                        data.toString());
    }
}
