package com.zhangke.websocket.util;

import android.util.Log;

/**
 * 日志工具类
 * <p>
 * Created by ZhangKe on 2019/3/21.
 */
public class LogUtil {

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        Log.v(tag, msg, tr);
    }

    public static void d(String tag, String text) {
        Log.d(tag, text);
    }

    public static void d(String tag, String text, Throwable tr) {
        Log.d(tag, text, tr);
    }

    public static void i(String tag, String text) {
        Log.i(tag, text);
    }

    public static void i(String tag, String text, Throwable tr) {
        Log.i(tag, text, tr);
    }

    public static void e(String tag, String text) {
        Log.e(tag, text);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    public static void w(String tag, Throwable tr) {
        Log.w(tag, tr);
    }

    public static void wtf(String tag, String msg) {
        Log.wtf(tag, msg);
    }

    public static void wtf(String tag, Throwable tr) {
        Log.wtf(tag, tr);
    }

    public static void wtf(String tag, String msg, Throwable tr) {
        Log.wtf(tag, msg, tr);
    }
}
