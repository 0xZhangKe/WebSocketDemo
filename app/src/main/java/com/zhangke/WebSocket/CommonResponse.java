package com.zhangke.WebSocket;

/**
 * 相应数据实体基类，不同的项目数据格式可能会有所不同，
 * 根据接口自己调整，大体上类似
 *
 * Created by ZhangKe on 2017/11/8.
 */

public class CommonResponse<T> {

    private String msg;
    private T data;
    private int code;
    private String path;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
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
