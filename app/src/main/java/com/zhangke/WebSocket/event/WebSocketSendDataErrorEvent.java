package com.zhangke.WebSocket.event;

import android.text.TextUtils;

/**
 * WebSocket 出错事件，一般是成功调用了接口，但是由于传参问题导致服务器返回了对应的错误信息
 *
 * Created by ZhangKe on 2017/11/8.
 */

public class WebSocketSendDataErrorEvent {
    private String path;//访问路径，可能为空
    private String response;//返回数据，可能为空
    private String cause;//原因

    public WebSocketSendDataErrorEvent(String path, String response, String cause) {
        this.path = path;
        this.response = response;
        this.cause = cause;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cause:");
        sb.append(cause);
        if(!TextUtils.isEmpty(path)){
            sb.append("---------path:");
            sb.append(path);
        }
        if(!TextUtils.isEmpty(response)){
            sb.append("---------response:");
            sb.append(response);
        }
        return sb.toString();
    }
}
