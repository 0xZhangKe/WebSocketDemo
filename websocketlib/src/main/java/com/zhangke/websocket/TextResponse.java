package com.zhangke.websocket;

/**
 * Created by ZhangKe on 2018/6/27.
 */
public class TextResponse implements Response<String> {

    private String responseText;
    private String responseEntity;

    public TextResponse(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(String responseEntity) {
        this.responseEntity = responseEntity;
    }
}
