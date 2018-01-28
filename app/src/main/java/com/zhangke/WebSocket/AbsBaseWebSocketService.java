package com.zhangke.WebSocket;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.neovisionaries.ws.client.HostnameUnverifiedException;
import com.neovisionaries.ws.client.OpeningHandshakeException;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.StatusLine;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.zhangke.WebSocket.event.DisconnectedEvent;
import com.zhangke.WebSocket.event.WebSocketConnectedEvent;
import com.zhangke.WebSocket.event.WebSocketConnectionErrorEvent;
import com.zhangke.WebSocket.event.WebSocketSendDataErrorEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 抽象类
 * <p>
 * <p>
 * Created by zk721 on 2018/1/28.
 */

public abstract class AbsBaseWebSocketService extends Service implements IWebSocket {

    private static final String TAG = "AbsBaseWebSocketService";
    private static final int TIME_OUT = 15000;
    private static WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(TIME_OUT);

    private AbsBaseWebSocketService.WebSocketThread webSocketThread;
    private WebSocket webSocket;

    private AbsBaseWebSocketService.ServiceBinder serviceBinder = new AbsBaseWebSocketService.ServiceBinder();

    public class ServiceBinder extends Binder {
        public AbsBaseWebSocketService getService() {
            return AbsBaseWebSocketService.this;
        }
    }

    private boolean stop = false;
    /**
     * 0-未连接
     * 1-正在连接
     * 2-已连接
     */
    private int connectStatus = 0;//是否已连接

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        ProxySettings settings = factory.getProxySettings();
        settings.addHeader("Content-Type", "text/json");

        connectStatus = 0;
        webSocketThread = new AbsBaseWebSocketService.WebSocketThread();
        webSocketThread.start();

        Log.i(TAG, "onCreated");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (serviceBinder == null) {
            serviceBinder = new AbsBaseWebSocketService.ServiceBinder();
        }
        Log.i(TAG, "onBind");
        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop = true;
        webSocket.disconnect();
        webSocket.flush();
        webSocket = null;
        connectStatus = 0;
        Log.i(TAG, "onDestroy");
    }

    /**
     * 获取服务器地址
     */
    protected abstract String getConnectUrl();

    /**
     * 分发响应数据
     */
    protected abstract void dispatchResponse(String textResponse);

    /**
     * 连接成功发送 WebSocketConnectedEvent 事件，
     * 请求成功发送 CommonResponse 事件，
     * 请求失败发送 WebSocketSendDataErrorEvent 事件。
     */
    private class WebSocketThread extends Thread {
        @Override
        public void run() {
            Log.i(TAG, "WebSocketThread->run()");
            setupWebSocket();
        }
    }

    private void setupWebSocket() {
        if (connectStatus != 0) return;
        connectStatus = 1;
        try {
            webSocket = factory.createSocket(getConnectUrl());
            webSocket.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    super.onTextMessage(websocket, text);
                    if (debug()) {
                        Log.i(TAG, String.format("onTextMessage->%s", text));
                    }
                    dispatchResponse(text);
                }

                @Override
                public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
                    super.onTextMessageError(websocket, cause, data);
                    Log.e(TAG, "onTextMessageError()", cause);
                    EventBus.getDefault().post(new WebSocketSendDataErrorEvent("", "", "onTextMessageError():" + cause.toString()));
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
                    EventBus.getDefault().post(new DisconnectedEvent());
                    Log.e(TAG, "onDisconnected()");
                    connectStatus = 0;
                    if (!stop) {
                        //断开之后自动重连
                        setupWebSocket();
                    }
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    super.onConnected(websocket, headers);
                    Log.i(TAG, "onConnected()");
                    connectStatus = 2;
                    EventBus.getDefault().post(new WebSocketConnectedEvent());
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                    super.onError(websocket, cause);
                    Log.e(TAG, "onError()", cause);
                    EventBus.getDefault().post(new WebSocketConnectionErrorEvent("onError:" + cause.getMessage()));
                }
            });
            try {
                webSocket.connect();
            } catch (NullPointerException e) {
                connectStatus = 0;
                Log.i(TAG, String.format("NullPointerException()->%s", e.getMessage()));
                Log.e(TAG, "NullPointerException()", e);
                EventBus.getDefault().post(new WebSocketConnectionErrorEvent("NullPointerException:" + e.getMessage()));
            } catch (OpeningHandshakeException e) {
                connectStatus = 0;
                Log.i(TAG, String.format("OpeningHandshakeException()->%s", e.getMessage()));
                Log.e(TAG, "OpeningHandshakeException()", e);
                StatusLine sl = e.getStatusLine();
                Log.i(TAG, "=== Status Line ===");
                Log.e(TAG, "=== Status Line ===");
                Log.i(TAG, String.format("HTTP Version  = %s\n", sl.getHttpVersion()));
                Log.e(TAG, String.format("HTTP Version  = %s\n", sl.getHttpVersion()));
                Log.i(TAG, String.format("Status Code   = %s\n", sl.getStatusCode()));
                Log.e(TAG, String.format("Status Code   = %s\n", sl.getStatusCode()));
                Log.i(TAG, String.format("Reason Phrase = %s\n", sl.getReasonPhrase()));
                Log.e(TAG, String.format("Reason Phrase = %s\n", sl.getReasonPhrase()));

                Map<String, List<String>> headers = e.getHeaders();
                Log.i(TAG, "=== HTTP Headers ===");
                Log.e(TAG, "=== HTTP Headers ===");
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    // Header name.
                    String name = entry.getKey();

                    // Values of the header.
                    List<String> values = entry.getValue();

                    if (values == null || values.size() == 0) {
                        // Print the name only.
                        System.out.println(name);
                        continue;
                    }

                    for (String value : values) {
                        // Print the name and the value.
                        Log.e(TAG, String.format("%s: %s\n", name, value));
                        Log.i(TAG, String.format("%s: %s\n", name, value));
                    }
                }
                EventBus.getDefault().post(new WebSocketConnectionErrorEvent("OpeningHandshakeException:" + e.getMessage()));
            } catch (HostnameUnverifiedException e) {
                connectStatus = 0;
                // The certificate of the peer does not match the expected hostname.
                Log.i(TAG, String.format("HostnameUnverifiedException()->%s", e.getMessage()));
                Log.e(TAG, "HostnameUnverifiedException()", e);
                EventBus.getDefault().post(new WebSocketConnectionErrorEvent("HostnameUnverifiedException:" + e.getMessage()));
            } catch (WebSocketException e) {
                connectStatus = 0;
                // Failed to establish a WebSocket connection.
                Log.i(TAG, String.format("WebSocketException()->%s", e.getMessage()));
                Log.e(TAG, "WebSocketException()", e);
                EventBus.getDefault().post(new WebSocketConnectionErrorEvent("WebSocketException:" + e.getMessage()));
            }
        } catch (IOException e) {
            connectStatus = 0;
            Log.i(TAG, String.format("IOException()->%s", e.getMessage()));
            Log.e(TAG, "IOException()", e);
            EventBus.getDefault().post(new WebSocketConnectionErrorEvent("IOException:" + e.getMessage()));
        }
    }

    @Override
    public void sendText(String text) {
        if (TextUtils.isEmpty(text)) return;
        if (debug()) {
            Log.i(TAG, String.format("sendText()->%s", text));
        }
        if (webSocket != null && connectStatus == 2) {
            webSocket.sendText(text);
        }
    }

    @Override
    public int getConnectStatus() {
        return connectStatus;
    }

    @Override
    public void reconnect() {
        Log.i(TAG, "reconnect()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "reconnect()->begin restart...");
                try {
                    Thread.sleep(200);
                }catch(Exception e){
                    Log.e(TAG, "reconnect()->run: ", e);
                }
                if (webSocketThread != null && !webSocketThread.isAlive()) {
                    connectStatus = 0;
                    webSocketThread = new WebSocketThread();
                    webSocketThread.start();
                    Log.i(TAG, "reconnect()->start success");
                } else {
                    Log.i(TAG, "reconnect()->start failed: webSocketThread==null || webSocketThread.isAlive()");
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        Log.i(TAG, "stop()");
        webSocket.disconnect();
        stop = true;
        Log.i(TAG, "stop()->success");
    }

    public boolean debug() {
        try {
            ApplicationInfo info = getApplication().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}