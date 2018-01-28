package com.zhangke.WebSocket.UI;

/**
 * 所有基类活动都需要实现这个接口
 *
 * Created by 张可 on 2017/5/23.
 */

public interface IBaseActivity {
    //使用Intent传递数据时的参数
    String INTENT_ARG_01 = "intent_01";
    String INTENT_ARG_02 = "intent_02";
    String INTENT_ARG_03 = "intent_03";
    String INTENT_ARG_04 = "intent_04";
    String INTENT_ARG_05 = "intent_05";

    void showRoundProgressDialog();
    void showRoundProgressDialog(String msg);
    void closeRoundProgressDialog();
    void showToastMessage(String msg);
}
