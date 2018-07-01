package com.zhangke.websocketdemo;

/**
 * 后台接口返回的数据格式
 * Created by ZhangKe on 2018/6/27.
 */
public class CommonResponseEntity {

    private String message;
    private String data;
    private int code;
    private String path;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
