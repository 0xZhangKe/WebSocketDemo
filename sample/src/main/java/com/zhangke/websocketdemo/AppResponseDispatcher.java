package com.zhangke.websocketdemo;

import android.util.Log;

import com.zhangke.websocket.SimpleDispatcher;
import com.zhangke.websocket.dispatcher.ResponseDelivery;
import com.zhangke.websocket.response.ErrorResponse;

/**
 * Created by ZhangKe on 2018/6/27.
 */
public class AppResponseDispatcher extends SimpleDispatcher {

    private static final String LOGTAG = "AppResponseDispatcher";

    @Override
    public void onMessage(String message, ResponseDelivery delivery) {
        CommonResponseEntity entity = new CommonResponseEntity();
        delivery.onMessage(message, entity);
    }

    /**
     * 统一处理错误信息，
     * 界面上可使用 ErrorResponse#getDescription() 来当做提示语
     */
    @Override
    public void onSendDataError(ErrorResponse error, ResponseDelivery delivery) {
        switch (error.getErrorCode()) {
            case ErrorResponse.ERROR_NO_CONNECT:
                error.setDescription("网络错误");
                break;
            case ErrorResponse.ERROR_UN_INIT:
                error.setDescription("连接未初始化");
                break;
            case ErrorResponse.ERROR_UNKNOWN:
                error.setDescription("未知错误");
                break;
            case 11:
                error.setDescription("数据格式异常");
                break;
        }
        delivery.onSendDataError(error);
    }
}
