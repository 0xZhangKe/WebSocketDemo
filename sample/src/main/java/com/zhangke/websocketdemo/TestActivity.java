package com.zhangke.websocketdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.WebSocketHandler;
import com.zhangke.websocket.WebSocketSetting;
import com.zhangke.websocket.response.ErrorResponse;

import org.java_websocket.framing.Framedata;

import java.nio.ByteBuffer;

public class TestActivity extends AppCompatActivity {

    private EditText etContent;
    private TextView tvMsg;
    private ScrollView scrollView;

    private SocketListener socketListener = new SocketListener() {
        @Override
        public void onConnected() {
            appendMsgDisplay("onConnected");
        }

        @Override
        public void onConnectFailed(Throwable e) {
            if(e != null){
                appendMsgDisplay("onConnectFailed:" + e.toString());
            }else{
                appendMsgDisplay("onConnectFailed:null");
            }
        }

        @Override
        public void onDisconnect() {
            appendMsgDisplay("onDisconnect");
        }

        @Override
        public void onSendDataError(ErrorResponse errorResponse) {
            appendMsgDisplay("onSendDataError:" + errorResponse.toString());
        }

        @Override
        public <T> void onMessage(String message, T data) {
            appendMsgDisplay("onMessage(String, T):" + message);
        }

        @Override
        public <T> void onMessage(ByteBuffer bytes, T data) {
            appendMsgDisplay("onMessage(ByteBuffer, T):" + bytes);
        }

        @Override
        public void onPing(Framedata framedata) {
            if(framedata != null){
                appendMsgDisplay("onPing:" + framedata.toString());
            }else{
                appendMsgDisplay("onPing:null");
            }
        }

        @Override
        public void onPong(Framedata framedata) {
            if(framedata != null){
                appendMsgDisplay("onPong:" + framedata.toString());
            }else{
                appendMsgDisplay("onPong:null");
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        initView();

        WebSocketHandler.getDefault().addListener(socketListener);

    }

    private void initView() {
        etContent = (EditText) findViewById(R.id.et_content);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etContent.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    UiUtil.showToast(TestActivity.this, "输入不能为空");
                    return;
                }
                WebSocketHandler.getDefault().send(text);
            }
        });
        findViewById(R.id.btn_reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebSocketSetting setting = WebSocketHandler.getDefault().getSetting();
                setting.setConnectUrl("new connect url");
                WebSocketHandler.getDefault().reconnect(setting);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketHandler.getDefault().removeListener(socketListener);
    }

    private void appendMsgDisplay(String msg){
        StringBuilder textBuilder = new StringBuilder();
        if(!TextUtils.isEmpty(tvMsg.getText())){
            textBuilder.append(tvMsg.getText().toString());
            textBuilder.append("\n");
        }
        textBuilder.append(msg);
        textBuilder.append("\n");
        tvMsg.setText(textBuilder.toString());
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
}
