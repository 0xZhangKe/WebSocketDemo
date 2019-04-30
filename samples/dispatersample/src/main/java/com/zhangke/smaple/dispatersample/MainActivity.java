package com.zhangke.smaple.dispatersample;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zhangke.websocket.SimpleListener;
import com.zhangke.websocket.SocketListener;
import com.zhangke.websocket.WebSocketHandler;
import com.zhangke.websocket.response.ErrorResponse;

import org.java_websocket.framing.Framedata;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private EditText etContent;
    private TextView tvMsg;
    private ScrollView scrollView;

    private SocketListener socketListener = new SimpleListener() {
        @Override
        public void onConnected() {
            appendMsgDisplay("onConnected");
        }

        @Override
        public void onConnectFailed(Throwable e) {
            if (e != null) {
                appendMsgDisplay("onConnectFailed:" + e.toString());
            } else {
                appendMsgDisplay("onConnectFailed:null");
            }
        }

        @Override
        public void onDisconnect() {
            appendMsgDisplay("onDisconnect");
        }

        @Override
        public void onSendDataError(ErrorResponse errorResponse) {
            appendMsgDisplay(errorResponse.getDescription());
            errorResponse.release();
        }

        @Override
        public <T> void onMessage(String message, T data) {
            if (data instanceof CommonResponseEntity) {
                CommonResponseEntity responseEntity = (CommonResponseEntity) data;
                appendMsgDisplay(responseEntity.getMessage());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    Toast.makeText(MainActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                WebSocketHandler.getDefault().send(text);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketHandler.getDefault().removeListener(socketListener);
    }

    private void appendMsgDisplay(String msg) {
        StringBuilder textBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(tvMsg.getText())) {
            textBuilder.append(tvMsg.getText().toString());
            textBuilder.append("\n");
        }
        textBuilder.append(msg);
        textBuilder.append("\n");
        tvMsg.setText(textBuilder.toString());
        tvMsg.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
