package com.zhangke.websocketdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.zhangke.websocket.SimpleListener;
import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.WebSocketHandler;
import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;

import org.java_websocket.framing.Framedata;

import java.nio.ByteBuffer;

public class LoginActivity extends AppCompatActivity {

    /**
     * 假设这是登陆的接口Path
     */
    private static final String LOGIN_PATH = "Login";

    private EditText etAccount;
    private EditText etPassword;
    private Button btnLogin;

    private SocketListener socketListener = new SimpleListener() {
        @Override
        public <T> void onMessage(T data) {
            super.onMessage(data);
            if (data instanceof CommonResponseEntity) {
                CommonResponseEntity entity = (CommonResponseEntity) data;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        WebSocketHandler.getDefault().addListener(socketListener);

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
                if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
                    UiUtil.showToast(LoginActivity.this, "输入不能为空");
                    return;
                }
                login(account, password);
            }
        });
    }

    private void login(String account, String password) {
        WebSocketHandler.getDefault().send("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketHandler.getDefault().removeListener(socketListener);
    }
}
