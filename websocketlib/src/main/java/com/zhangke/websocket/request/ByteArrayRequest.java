package com.zhangke.websocket.request;

import android.support.annotation.NonNull;

import org.java_websocket.client.WebSocketClient;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * byte[] 类型的请求
 * <p>
 * Created by ZhangKe on 2019/3/22.
 */
public class ByteArrayRequest implements Request<byte[]> {

    private static Queue<ByteArrayRequest> CACHE_QUEUE = new ArrayDeque<>(10);

    public static ByteArrayRequest obtain() {
        ByteArrayRequest request = CACHE_QUEUE.poll();
        if (request == null) {
            request = new ByteArrayRequest();
        }
        return request;
    }

    public static void release(ByteArrayRequest request) {
        CACHE_QUEUE.offer(request);
    }

    private byte[] data;

    @Override
    public void setRequestData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getRequestData() {
        return this.data;
    }

    @Override
    public void send(WebSocketClient client) {
        client.send(this.data);
    }

    @NonNull
    @Override
    public String toString() {
        if (data != null) {
            return "[byte[],length=" + data.length + "]";
        } else {
            return "[byte[],data is null";
        }
    }
}
