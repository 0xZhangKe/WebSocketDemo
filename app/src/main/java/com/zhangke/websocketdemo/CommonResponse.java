package com.zhangke.websocketdemo;

import com.zhangke.websocket.Response;

/**
 * Created by ZhangKe on 2018/6/27.
 */
public class CommonResponse implements Response<CommonResponseEntity> {

    private String responseText;
    private CommonResponseEntity responseEntity;

    public CommonResponse(String responseText, CommonResponseEntity responseEntity) {
        this.responseText = responseText;
        this.responseEntity = responseEntity;
    }

    @Override
    public String getResponseText() {
        return responseText;
    }

    @Override
    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    @Override
    public CommonResponseEntity getResponseEntity() {
        return this.responseEntity;
    }

    @Override
    public void setResponseEntity(CommonResponseEntity responseEntity) {
        this.responseEntity = responseEntity;
    }
}
