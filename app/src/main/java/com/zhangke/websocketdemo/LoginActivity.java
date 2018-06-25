package com.zhangke.websocketdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.zhangke.websocket.AbsBaseWebSocketActivity;

public class LoginActivity extends AbsBaseWebSocketActivity<WebSocketService> {

    /**
     * 假设这是登陆的接口Path
     */
    private static final String LOGIN_PATH = "path_login";

    private EditText etAccount;
    private EditText etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        etAccount = (EditText) findViewById(R.id.et_account);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnLogin = (Button) findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                if(TextUtils.isEmpty(account) || TextUtils.isEmpty(password)){
                    UiUtil.showToast(LoginActivity.this, "输入不能为空");
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
        sendText(param.toString());
    }

    @Override
    protected void onMessageResponse(String message) {
        if (TextUtils.isEmpty(message)) {
            UiUtil.showToast(LoginActivity.this, "登陆成功");
        }
    }

    @Override
    protected void onSendMessageError(String error) {
        UiUtil.showToast(LoginActivity.this, "登陆失败：%s");
    }
}
