package com.zhangke.websocketdemo;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by ZhangKe on 2018/6/25.
 */
public class UiUtil {

    private static Toast toast;

    public static void showToast(final Context context, final String message) {
        showToast(context, message, false);
    }

    public static void showToast(final Context context, final String message, final boolean center) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        if (center) {
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        toast.show();
    }
}
