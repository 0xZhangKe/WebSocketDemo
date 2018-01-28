## WebSocketDemo
 WebSocket 安卓客户端的实现方式。
## 介绍
如果不想了解其中的原理可以直接拉到最后面的使用方式章节，按照教程使用即可，或者直接打开 demo 查看代码。</p>

本文使用一个后台 Service 来建立 WebSocket 连接，Activity 与 Fragment 需要使用 WebSocket 接口时只需要绑定该服务既可。</p>
WebSocketService 接收到数据之后会通知每一个绑定了 WebSocketService 服务的 Activity 及 Fragment，其自身也可以对返回的数据进行判断等等，具体如何操作根据业务需求定。</p>
下图是 WebSocketService 的工作流程图</p>

![](http://otp9vas7i.bkt.clouddn.com/WebSocketService%E5%B7%A5%E4%BD%9C%E6%B5%81%E7%A8%8B.png)</p>

WebSocketService 负责建立 WebSocket 连接，接收返回的数据，接收到的数据通过 EventBus 发送出去，连接失败之后可以自动重连。</p>
下图是 Activity 的工作流程图</p>

![](http://otp9vas7i.bkt.clouddn.com/Activity%E6%B5%81%E7%A8%8B.png)</p>

Activity/Fragment 绑定 WebSocket 服务，绑定成功后可以直接调用 WebSocketService 对象发送数据。</p>
## WebSocketService
### 添加必要的依赖
首先添加 WebSocket 框架依赖：
```
implementation 'org.greenrobot:eventbus:3.0.0'
```
这个框架也是我在 Github 上找了一圈之后选中的一个，使用的人很多，文档齐全，还在继续维护。</p>
另外还要添加一个阿里的 JSON 框架：
```
implementation 'com.alibaba:fastjson:1.2.33'
```
好了完事大吉，现在开始吧。</p>

### 定义 WebSocket 提供的接口
先创建一个 WebSocket 的接口，其中定义了 WebSocket 必须提供的几个公开方法：</p>
```java
public interface IWebSocket {

    /**
     * 发送数据
     *
     * @param text 需要发送的数据
     */
    void sendText(String text);

    /**
     * 0-未连接
     * 1-正在连接
     * 2-已连接
     */
    int getConnectStatus();

    /**
     * 重新连接
     */
    void reconnect();

    /**
     * 关闭连接
     */
    void stop();
}

```
WebSocketService 需要实现这个接口，后面绑定 WebSocketService 时直接通过 IWebSocket 创建对象既可。
### AbsWebSocketService
我这里为了降低代码的耦合度，将与业务逻辑相关的代码（接口地址、数据处理及分发等）与 WebSocket 的连接、发送数据等操作剥离开来，所以这里创建的时一个抽象类 AbsWebSocketService 来实现与业务逻辑无关的代码。</p>
在实际使用中只需要创建一个 WebSocketService 并继承该 AbsWebSocketService 既可，不需要改动其中的代码。</p>
首先看一下 AbsWebSocketService 的代码：
```java
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
```
其中有两个抽象方法：</p>
```
String getConnectUrl()//获取服务器连接地址
void dispatchResponse(String textResponse)//接收到数据后回调此方法，在此方法中分发数据
```
创建好上面的 AbsWebSocketService 服务之后，还需要根据业务需求创建一个 WebSocketService 实现该类。</p>
### WebSocketService 服务
这个代码就很简单了，如下：
```java
public class WebSocketService extends AbsBaseWebSocketService {

    @Override
    protected String getConnectUrl() {
        return "服务器对应的url";
    }

    @Override
    protected void dispatchResponse(String textResponse) {
        //处理数据
        try {
            CommonResponse<String> response = JSON.parseObject(textResponse, new TypeReference<CommonResponse<String>>() {
            });
            if (response == null) {
                EventBus.getDefault().post(new WebSocketSendDataErrorEvent("", textResponse, "响应数据为空"));
                return;
            }
            //此处可根据服务器接口文档进行调整，判断 code 值是否合法，如下：
//            if (response.getCode() >= 1000 && response.getCode() < 2000) {
//                EventBus.getDefault().post(response);
//            }else{
//                EventBus.getDefault().post(new WebSocketSendDataErrorEvent(response.getCommand().getPath(), textResponse, response.getMsg()));
//            }
            EventBus.getDefault().post(response);
        }catch(Exception e){
            //一般由于 JSON 解析时出现异常
            EventBus.getDefault().post(new WebSocketSendDataErrorEvent("", textResponse, "数据异常:" + e.getMessage()));
        }
    }
}
```
dispatchResponse(String) 方法中就是将数据转换成对应的实体，然后使用 EventBus 将其发送出去，可以再其中做一些数据正确的判断，比如上面注释的地方。</p>
其中的 CommonResponse 是我们后台接口的一个标准模板，所有格接口返回的数据都应该按照这个格式来，这个类就按照自家的接口写就行了，不用按照我的。看一下其中的代码：</p>
```java
public class CommonResponse<T> {

    private String msg;
    private T data;
    private int code;
    private String path;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

```
其中 path 表示接口地址，其本质就是个字符串，我们通过这个字符串当做一个标识符，标识返回的数据属于哪个接口，然后我们才能做出对应的操作。</p>
泛型 T 表示数据的实体，一般来说我们会按照不同的接口写出不同的实体方便使用，当然了，这些都不重要，也只是我的个人习惯，这里也不涉及核心代码，所以可以根据个人爱好随意改动。</p>
别忘了在 AndroidManifest 中注册该服务，然后在合适的时候启动该服务，我的是在 Application 中的 onCreate 方法启动的：
```java
public class GateApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, WebSocketService.class);
        startService(intent);
    }
}
```
到了这里 WebSocket 服务就已经介绍完了，但是我们如果直接这么用肯定很麻烦。
比如当调用 WebSocketService.sendText(String) 方法时发现 WebSocket 连接已经断了、绑定 WebSocket 服务、判断其连接状态等等，还有很多事情要做，总不能每个 Activity/Fragment 都要写这么多代码去判断吧。</p>
为此我又写了 AbsBaseWebSocketActivity 与 AbsBaseWebSocketFragment 两个抽象类，其中屏蔽掉了大部分的连接状态判断等等操作。
比如我们调用 AbsBaseWebSocketFragment.sendText(String) 方法时，可以直接判断出当前时候是连接状态，如果未连接则重新连接，连接完成后再去发送数据。</p>
先来看一下 ABSBaseWebSocketActivity 的代码：
### AbsBaseWebSocketActivity
其中主要包括绑定服务，判断连接状态，发送数据等操作，另外暴露出了几个方法以供使用：
```java
public abstract class AbsBaseWebSocketActivity extends BaseAppCompatActivity {
    /**
     * 服务重连次数，
     * 这里指的是绑定 WebSocket 服务失败时使用的重连次数，一般来说不会出现绑定失败的情况
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

    private boolean isConnected = false;
    private boolean networkReceiverIsRegister = false;
    private int connectTime = 0;
    protected ServiceConnection mWebSocketServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected()");
            mWebSocketService = (IWebSocket) ((AbsBaseWebSocketService.ServiceBinder) service).getService();
            //此处假设要不就已经连接，要不就未连接，未连接就等着接收连接成功/失败的广播即可
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
     * 断开则调用 reconnect 方法重连。
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mWebSocketService != null)
            Log.e(TAG, "-----------------ConnectStatus" + mWebSocketService.getConnectStatus());
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

    protected abstract Class<? extends AbsBaseWebSocketService> getWebSocketClass();

    /**
     * 绑定服务，
     * 进入该界面时绑定服务，
     * 绑定失败则继续绑定，知道超过设定的次数为止。
     */
    protected void bindWebSocketService() {
        Intent intent = new Intent(this, getWebSocketClass());
        bindService(intent, mWebSocketServiceConnection, Context.BIND_AUTO_CREATE);
        connectTime++;
        Log.i(TAG, "bindWebSocketService() success");
    }

    protected abstract void onCommonResponse(CommonResponse<String> response);

    protected abstract void onErrorResponse(WebSocketSendDataErrorEvent response);

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
            Log.i(TAG, "sendText()->已连接，直接发送数据");
            //已连接，直接发送数据
            mWebSocketService.sendText(text);
        } else {
            //未连接，先连接，再发送数据
            Log.i(TAG, "sendText()->未连接");
            connectType = 2;
            needSendText = text;
            if (mWebSocketService.getConnectStatus() == 0) {
                Log.i(TAG, "sendText()->建立连接");
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
        Log.e(TAG, String.format("onEventMainThread(WebSocketSendDataErrorEvent)->%s", event.toString()));
        onErrorResponse(event);
    }

    /**
     * 连接成功
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(WebSocketConnectedEvent event) {
        isConnected = true;
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
        Log.e(TAG, String.format("onEventMainThread(WebSocketConnectionErrorEvent)->onConnectFailed:%s", event.toString()));
        closeRoundProgressDialog();
        showToastMessage(networkErrorTips);
        connectType = 0;
        onConnectFailed();
    }

    @Override
    protected void onDestroy() {
        unbindService(mWebSocketServiceConnection);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
```
看一下其中的抽象方法：
```
Class<? extends AbsBaseWebSocketService> getWebSocketClass();//获取 WebSocketService 类，这里传入 WebSocketService.class 既可
void onCommonResponse(CommonResponse<String> response);//当有接收到数据时会回调此方法
void onErrorResponse(WebSocketSendDataErrorEvent response);//当有发送数据失败时会回调此方法
```
这样一个 WebSocket 的功能就已经实现了，现在来说一下怎么使用。
### 使用方式
直接使需要的 Activity 继承 ABSBaseWebSocketActivity，调用 sendText(String) 方法既可发送数据，接收到数据后会回调 onCommonResponse(CommonResponse<String>) 方法或 onErrorResponse(WebSocketSendDataErrorEvent) 方法。</p>
下面用一个使用案例更直观一点：</p>
假设现在要在 LoginActivity 中实现登陆功能，首先创建 LoginActivity，并初始化控件：
```java
public class LoginActivity extends AbsBaseWebSocketActivity {

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

    }

    @Override
    protected Class<? extends AbsBaseWebSocketService> getWebSocketClass() {
        return WebSocketService.class;
    }

    @Override
    protected void onCommonResponse(CommonResponse<String> response) {

    }

    @Override
    protected void onErrorResponse(WebSocketSendDataErrorEvent response) {

    }
}
```
上面的代码就是很简单的初始化控件，监听按键输入。
其中的 login(String, String) 方法是空的，现在我们来完成 login 方法：
```java
    private void login(String account, String password){
        JSONObject param = new JSONObject();
        param.put("account", account);
        param.put("password", password);
        param.put("path", LOGIN_PATH);
        sendText(param.toString());//调用 WebSocket 发送数据
        showRoundProgressDialog();//显示加载对话框
    }
```
以及获取返回数据：
```java
    /**
     * 登陆成功
     */
    @Override
    protected void onCommonResponse(CommonResponse<String> response) {
        closeRoundProgressDialog();//关闭加载对话框
        showToastMessage("登陆成功");
    }

    /**
     * 调用接口出错或接口提示错误
     */
    @Override
    protected void onErrorResponse(WebSocketSendDataErrorEvent response) {
        closeRoundProgressDialog();//关闭加载对话框
        showToastMessage(String.format("登陆失败：%s", response));
    }
```
下面来看一下完整的 LoginActivity 代码：
```java
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
```
按照上面所示就可以完成一次 WebSocket 的接口调用。</p>
另外还有一点需要注意的，考虑这样的一种情况，比如我们在打开登陆界面时需要初始化一些数据，如果是 HTTP 接口我们可以直接在 onCreate 方法中获取数据就行了，但是使用 WebSocket 就没办法在 onCreate 去调用，因为打开一个新的 Activity 时我们需要先绑定 WebSocketService 服务，我们得在绑定完成后才能调用 WebSocket 接口。</p>
ABSBaseWebSocketActivity 中提供了一个 onServiceBindSuccess() 方法，这个方法就是绑定成功后的回调方法，我们可以再这个方法中初始化一些数据。</p>
**PS：我们可以在创建一个 BaseWebSocketServiceActivity 抽象类，实现其中的 Class<? extends AbsBaseWebSocketService> getWebSocketClass() 方法，因为在同一个 APP 中这个方法的返回值是一直不变的。**
到此关于如何在安卓上实现一个 WebSocket 客户端就介绍完了，有问题欢迎讨论。