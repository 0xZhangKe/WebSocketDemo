package com.zhangke.websocket.response;

import android.support.annotation.NonNull;

import com.zhangke.websocket.util.TextUtil;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 默认的消息响应事件包装类，
 * 只包含文本，不包含数据实体
 * Created by ZhangKe on 2018/6/27.
 */
public class TextResponse implements Response<String> {

    private static Queue<TextResponse> pool = new ArrayDeque<>(10);

    private String responseText;

    /**
     * 获取一个 Response
     */
    public static TextResponse obtain() {
        TextResponse response = pool.poll();
        if (response == null) {
            response = new TextResponse();
        }
        return response;
    }

    /**
     * 回收一个 Response
     */
    public static void release(TextResponse response) {
        pool.offer(response);
    }

    private TextResponse() {
    }

    @Override
    public String getResponseData() {
        return responseText;
    }

    @Override
    public void setResponseData(String responseData) {
        this.responseText = responseData;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("[@TextResponse%s->responseText:%s]",
                hashCode(),
                TextUtil.isEmpty(responseText) ? "null" : responseText);
    }
}
