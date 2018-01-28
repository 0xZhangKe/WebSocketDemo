package com.zhangke.WebSocket.UI;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by 张可 on 2017/5/22.
 */

public class RoundProgressDialog {
    private ProgressDialog progressDialog;
    private Context context;

    private RoundProgressDialog(){
    }

    public RoundProgressDialog(Context context){
        this.context=context;
    }

    /**
     * 显示进度条
     */
    public  void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(context);
            progressDialog.setMessage("请稍等...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    public boolean isShowing(){
        if(null == progressDialog) return false;
        return progressDialog.isShowing();
    }

    /**
     * 显示进度条
     */
    public  void showProgressDialog(String msg){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(context);
            progressDialog.setMessage(msg);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度条
     */
    public  void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
