package com.zhangke.WebSocket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.zhangke.WebSocket.UI.BaseFragment;
import com.zhangke.WebSocket.event.WebSocketConnectedEvent;
import com.zhangke.WebSocket.event.WebSocketConnectionErrorEvent;
import com.zhangke.WebSocket.event.WebSocketSendDataErrorEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 简化了 WebSocket 相关操作的 Fragment 基类
 *
 * Created by zk721 on 2018/1/28.
 */

public abstract class AbsBaseWebSocketFragment extends BaseFragment {

    /**
     * 服务重连次数
     */
    private final int RECONNECT_TIME = 5;

    private IWebSocket mWebSocketService;
    protected String networkErrorTips;

    /**
     * 连接时机：</br>
     * 0 - 刚进入界面时，如果 WebSocket 还未连接，会继续连接，或者由于某些原因 WebSocket 断开，会自动重连，从而会触发连接成功/失败事件；</br>
     * 1 - onResume() 方法回调时判断 WebSocket 是否连接，如果未连接，则进行连接，从而触发连接成功/失败事件；</br>
     * 2 - sendText() 方法会判断 WebSocket 是否已经连接，如果未连接，则进行连接，从而触发连接成功/失败事件，此时连接成功后应继续调用 sendText() 方法发送数据。</br>
     * <p>
     * 另外，当 connectType != 0 时，每次使用完之后应该设置为 0。因为 0 的状态是无法预知的，随时可能调用。
     */
    private int connectType = 0;
    /**
     * 需要发送的数据，当 connectType == 2 时会使用。
     */
    private String needSendText;

    private int connectTime = 0;
    protected ServiceConnection mWebSocketServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected()");
            mWebSocketService = (IWebSocket) ((AbsBaseWebSocketService.ServiceBinder) service).getService();
            if (mWebSocketService.getConnectStatus() == 2) {
                Log.i(TAG, "onServiceConnected()->mWebSocketService.getConnectStatus() == 2; BindSuccess");
                onServiceBindSuccess();
            } else {
                Log.i(TAG, String.format("onServiceConnected()->mWebSocketService.getConnectStatus() == %s", mWebSocketService.getConnectStatus()));
                if (mWebSocketService.getConnectStatus() == 0) {
                    Log.i(TAG, "onServiceConnected()->mWebSocketService.getConnectStatus() == 0; mWebSocketService.restartThread()");
                    mWebSocketService.reconnect();
                }
                showRoundProgressDialog();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected()");
            mWebSocketService = null;
            if (connectTime <= RECONNECT_TIME) {
                Log.i(TAG, "onServiceDisconnected()->retry bindWebSocketService()");
                bindWebSocketService();
            }
        }
    };

    @Override
    protected void initBind() {
        super.initBind();
        networkErrorTips = "网络错误";
        EventBus.getDefault().register(this);
        bindWebSocketService();
    }

    /**
     * 从后台返回时，判断服务是否已断开，
     * 断开则调用 reStartThread 方法重连。
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mWebSocketService != null && mWebSocketService.getConnectStatus() != 2) {
            Log.i(TAG, "onResume()->WebSocket 未连接");
            showRoundProgressDialog();
            if (mWebSocketService.getConnectStatus() == 0) {
                Log.i(TAG, "onResume()->WebSocket 尝试重新连接 restartThread()");
                mWebSocketService.reconnect();
            }else{
                Log.i(TAG, "onResume()->WebSocket 正在连接");
            }
            connectType = 1;
        }
    }

    /**
     * 绑定服务，
     * 进入该界面时绑定服务，
     * 绑定失败则继续绑定，知道超过设定的次数为止。
     */
    protected void bindWebSocketService() {
        Intent intent = new Intent(mActivity, getWebSocketClass());
        mActivity.getApplicationContext().bindService(intent, mWebSocketServiceConnection, Context.BIND_AUTO_CREATE);
        connectTime++;
        Log.i(TAG, "bindWebSocketService()");
    }

    protected abstract void onCommonResponse(CommonResponse<String> response);

    protected abstract void onErrorResponse(WebSocketSendDataErrorEvent response);

    protected abstract Class<? extends AbsBaseWebSocketService> getWebSocketClass();

    /**
     * 连接失败
     */
    protected void onConnectFailed() {
        Log.i(TAG, "onConnectFailed()");
    }

    protected IWebSocket getWebSocketService() {
        return mWebSocketService;
    }

    /**
     * 服务绑定成功后回调改方法，可以在此方法中加载一些初始化数据
     */
    protected void onServiceBindSuccess() {
        Log.i(TAG, "onServiceBindSuccess()");
    }

    /**
     * 发送数据
     */
    protected void sendText(String text) {
        if (mWebSocketService.getConnectStatus() == 2) {
            //已连接，直接发送数据
            Log.i(TAG, "sendText()->已连接，直接发送数据");
            mWebSocketService.sendText(text);
        } else {
            //未连接，先连接，再发送数据
            Log.i(TAG, "sendText()->未连接");
            connectType = 2;
            needSendText = text;
            if (mWebSocketService.getConnectStatus() == 0) {
                Log.i(TAG, "sendText()->重新建立连接...");
                mWebSocketService.reconnect();
            }
        }
    }

    /**
     * 发送数据失败或者数据返回不合规
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommonResponse<String> event) {
        onCommonResponse(event);
    }

    /**
     * 发送数据失败或者数据返回不合规（code >=2000等）
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(WebSocketSendDataErrorEvent event) {
        Log.i(TAG, String.format("onEventMainThread(WebSocketSendDataErrorEvent)->%s", event.toString()));
        Log.e(TAG, String.format("onEventMainThread(WebSocketSendDataErrorEvent)->%s", event.toString()));
        onErrorResponse(event);
    }

    /**
     * 连接成功
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(WebSocketConnectedEvent event) {
        if (connectType == 2 && !TextUtils.isEmpty(needSendText)) {
            Log.i(TAG, "onEventMainThread(WebSocketConnectedEvent) -> sendText()");
            sendText(needSendText);
        } else if (connectType == 0) {
            Log.i(TAG, "onEventMainThread(WebSocketConnectedEvent) -> onServiceBindSuccess()");
            closeRoundProgressDialog();
            onServiceBindSuccess();
        }
        connectType = 0;
    }

    /**
     * 连接失败
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(WebSocketConnectionErrorEvent event) {
        Log.i(TAG, String.format("onEventMainThread(WebSocketConnectionErrorEvent)->onConnectFailed:%s", event.toString()));
        Log.e(TAG, String.format("onEventMainThread(WebSocketConnectionErrorEvent)->onConnectFailed:%s", event.toString()));
        closeRoundProgressDialog();
        showToastMessage(networkErrorTips);
        connectType = 0;
        onConnectFailed();
    }

    @Override
    public void onDestroy() {
        mActivity.getApplicationContext().unbindService(mWebSocketServiceConnection);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}