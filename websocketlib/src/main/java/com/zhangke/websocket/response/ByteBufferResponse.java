package com.zhangke.websocket.response;

import android.support.annotation.NonNull;

import com.zhangke.websocket.dispatcher.IResponseDispatcher;
import com.zhangke.websocket.dispatcher.ResponseDelivery;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 接收到二进制数据
 * <p>
 * Created by ZhangKe on 2019/3/22.
 */
public class ByteBufferResponse implements Response<ByteBuffer> {

    private ByteBuffer data;

    ByteBufferResponse() {
    }

    @Override
    public ByteBuffer getResponseData() {
        return data;
    }

    @Override
    public void setResponseData(ByteBuffer responseData) {
        this.data = responseData;
    }

    @Override
    public void onResponse(IResponseDispatcher dispatcher, ResponseDelivery delivery) {
        dispatcher.onMessageResponse(data, delivery);
        release();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("[@ByteBufferResponse%s->ByteBuffer:%s]",
                hashCode(),
                data == null ?
                        "null" :
                        data.toString());
    }

    @Override
    public void release() {
        ResponseFactory.releaseByteBufferResponse(this);
    }
}
