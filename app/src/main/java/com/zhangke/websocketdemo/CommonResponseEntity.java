package com.zhangke.websocketdemo;

/**
 * 后台接口返回的数据格式
 * Created by ZhangKe on 2018/6/27.
 */
public class CommonResponseEntity {

    private String msg;
    private String data;
    private int code;//10开头成功
    private CommandBean command;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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

    public CommandBean getCommand() {
        return command;
    }

    public void setCommand(CommandBean command) {
        this.command = command;
    }

    public static class CommandBean {
        /**
         * path : employee.consumer.Login
         */

        private String path;
        private String unique;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getUnique() {
            return unique;
        }

        public void setUnique(String unique) {
            this.unique = unique;
        }
    }
}
