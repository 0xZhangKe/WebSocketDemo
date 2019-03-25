package com.zhangke.websocket.response;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 接收到二进制数据
 * <p>
 * Created by ZhangKe on 2019/3/22.
 */
public class ByteBufferResponse implements Response<ByteBuffer> {

    private static Queue<ByteBufferResponse> CACHE_QUEUE = new ArrayDeque<>(10);

    private ByteBuffer data;

    /**
     * 获取一个 Response
     */
    public static ByteBufferResponse obtain() {
        ByteBufferResponse response = CACHE_QUEUE.poll();
        if (response == null) {
            response = new ByteBufferResponse();
        }
        return response;
    }

    /**
     * 回收一个 Response
     */
    public static void release(ByteBufferResponse response) {
        CACHE_QUEUE.offer(response);
    }

    private ByteBufferResponse() {
    }

    @Override
    public ByteBuffer getResponseData() {
        return data;
    }

    @Override
    public void setResponseData(ByteBuffer responseData) {
        this.data = responseData;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("[@ByteBufferResponse%s->ByteBuffer:%s]",
                hashCode(),
                data == null ? "null" : data.toString());
    }
}
