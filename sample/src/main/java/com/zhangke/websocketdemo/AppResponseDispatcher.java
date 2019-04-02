package com.zhangke.websocketdemo;

import android.util.Log;

import com.zhangke.websocket.SimpleDispatcher;
import com.zhangke.websocket.dispatcher.ResponseDelivery;
import com.zhangke.websocket.response.ErrorResponse;

/**
 * Created by ZhangKe on 2018/6/27.
 */
public class AppResponseDispatcher<T> extends SimpleDispatcher<T> {

    private static final String LOGTAG = "AppResponseDispatcher";

    @Override
    public void onMessage(String message, ResponseDelivery delivery) {
        CommonResponseEntity entity = new CommonResponseEntity();
        delivery.onMessage(entity);
    }

    /**
     * 统一处理错误信息，
     * 界面上可使用 ErrorResponse#getDescription() 来当做提示语
     */
    @Override
    public void onSendDataError(ErrorResponse error, ResponseDelivery delivery) {
        switch (error.getErrorCode()) {
            case 1:
                error.setDescription("网络错误");
                break;
            case 2:
                error.setDescription("网络错误");
                break;
            case 3:
                error.setDescription("网络错误");
                break;
            case 11:
                error.setDescription("数据格式异常");
                Log.e(LOGTAG, "数据格式异常", error.getCause());
                break;
        }
        delivery.onSendDataError(error);
    }
}
