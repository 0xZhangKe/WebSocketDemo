package com.zhangke.websocketdemo;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.zhangke.WebSocket.AbsBaseWebSocketActivity;
import com.zhangke.WebSocket.AbsBaseWebSocketService;
import com.zhangke.WebSocket.CommonResponse;
import com.zhangke.WebSocket.event.WebSocketSendDataErrorEvent;

public class LoginActivity extends AbsBaseWebSocketActivity {

    /**
     * 假设这是登陆的接口Path
     */
    private static final String LOGIN_PATH = "path_login";

    private EditText etAccount;
    private EditText etPassword;
    private Button btnLogin;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        etAccount = (EditText) findViewById(R.id.et_account);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnLogin = (Button) findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                if(TextUtils.isEmpty(account) || TextUtils.isEmpty(password)){
                    showToastMessage("输入不能为空");
                    return;
                }
                login(account, password);
            }
        });
    }

    private void login(String account, String password){
        JSONObject param = new JSONObject();
        param.put("account", account);
        param.put("password", password);
        param.put("path", LOGIN_PATH);
        sendText(param.toString());//调用 WebSocket 发送数据
        showRoundProgressDialog();//显示加载对话框
    }

    /**
     * 登陆成功
     */
    @Override
    protected void onCommonResponse(CommonResponse<String> response) {
        if (response != null && !TextUtils.isEmpty(response.getPath()) && TextUtils.equals(LOGIN_PATH, response.getPath())) {
            //我们需要通过 path 判断是不是登陆接口返回的数据，因为也有可能是其他接口返回的
            closeRoundProgressDialog();//关闭加载对话框
            showToastMessage("登陆成功");
        }
    }

    /**
     * 调用接口出错或接口提示错误
     */
    @Override
    protected void onErrorResponse(WebSocketSendDataErrorEvent response) {
        closeRoundProgressDialog();//关闭加载对话框
        showToastMessage(String.format("登陆失败：%s", response));
    }

    @Override
    protected Class<? extends AbsBaseWebSocketService> getWebSocketClass() {
        return WebSocketService.class;//这里传入 WebSocketService 既可
    }
}
