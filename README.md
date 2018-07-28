## 介绍
关于 WebSocket Android 端的使用封装之前已经做过一次了，但在使用了一段时间之后逐渐发现了一些问题，一直想改也没时间，正好最近公司业务比较少，就趁着这段时间有空闲把代码优化了一下，其实差不多是重新做一套了。
这个版本的使用方式上比之前简化了很多，集成起来也更容易，并且代码逻辑更加清晰，模块与模块之间的耦合降到最低，运行效率更高，更健壮，好了废话不说了，先介绍一下使用方式。

## 如何使用
首先将代码集成到自己的项目中，这里有两种集成方式，第一种是使用 Gradle 依赖这个项目既可，第二种把代码拷贝到自己项目中，我建议使用第二种方式，这样你觉得有什么问题自己改起来比较方便，当然了也可以直接给我提 [issue](https://github.com/0xZhangKe/WebSocketDemo/issues/new) 我来改。
### 集成
#### Gradle 方式集成
在对应 model 的  build.gradle 中添加依赖：
```gradle
implementation 'com.github.0xZhangKe:WebSocketDemo:2.2'
```
然后 sync 一下，如果出现类似的错误：
```
Failed to resolve: com.github.0xZhangKe:WebSocketDemo:2.2
```
那意味着你还没添加 Github 的仓库，到项目根目录中的 build.gradle 中添加如下代码：
```gradle
maven { url = 'https://jitpack.io' }
```
#### 第二种集成方式
这个就很简单了，直接把 websocketlib 中的代码拷贝到自己的项目中就行，具体怎么做就看你的个人喜好。

### 相关配置
按照上面的步骤集成进来之后再做一些简单的配置可以使用了。

### 配置 WebSocket 连接地址
首先，最重要的一点，配置 WebSocket 连接地址：
```java
WebSocketSetting.setConnectUrl("Your WebSocket connect url");
```
这一步必须在启动 WebSocketService 使用前调用，我是在 Application 中配置的，建议你们也这么做，可以看一下[ demo ](https://github.com/0xZhangKe/WebSocketDemo/blob/master/app/src/main/java/com/zhangke/websocketdemo/App.java)的使用方式。
这一步配置完成后一个简单的 WebSocketService 就可以使用了。

### 配置统一的消息处理器
在我们实际开发中可能需要考虑更多的问题，比如数据格式的统一规划，后台返回数据的统一处理，处理完成后再发送到下游等等。

机智的我早就想到了解决方案，本项目中使用 [IResponseDispatcher](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/IResponseDispatcher.java) 来分发数据，可以看到这是个接口，默认会使用 [DefaultResponseDispatcher](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/DefaultResponseDispatcher.java) 来当做消息分发器，如果不进行设置 WebSocket 接收到数据后会直接发送给下游。

那么我们先来看一下 IResponseDispatcher：
```java
public interface IResponseDispatcher {

    //省略掉其他代码

    /**
     * 接收到消息
     *
     * @param message 接收到的消息
     * @param delivery 消息发射器
     */
    void onMessageResponse(Response message, ResponseDelivery delivery);

    //省略掉其他代码

}
```
IResponseDispatcher 共中有五个方法需要实现，大体上都类似的，我们只看其中一个就行。

onMessageResponse 方法中的两个参数，Response 后面会介绍，这里说一下 ResponseDelivery，我管它叫消息发射器，其实很简单，他内部就是维护了一个监听器的 List，当调用其中某个方法时会遍历调用所有的 Listener 中对应的方法。
当我们处理完数据之后通过这个就可以将数据发送到下游的 Activity/Fragment 中，很简单的吧，当然也可以对消息进行拦截，或者将数据包装成统一的格式再发送出去。
举个栗子，我们要将数据转成统一的一个实体在发送到下游，那么在实现类中可以这么做：
```java
    @Override
    public void onMessageResponse(Response message, ResponseDelivery delivery) {
         delivery.onMessageResponse(new CommonResponse(message.getResponseText(), JSON.parseObject(message.getResponseText(), new TypeReference<CommonResponseEntity>() {
         })));
    }
```
上面是把 Response 中的消息数据转成我们根据后台数据统一格式自定义的 CommonResponseEntity 对象再包装成一个自定义的 CommonResponse 对象发送出去。
除此之外，更重要的一点是，当我们将消息数据转成 CommonResponseEntity 之后可以根据业务逻辑来进行统一的处理，例如后台规定返回数据中的 code 字段等于 1000 时才代表接口调用成功，那么我们就可以直接在这里做判断了，而不是每个地方都要判断一次：
```java
    @Override
    public void onMessageResponse(Response message, ResponseDelivery delivery) {
        try {
            CommonResponse commonResponse = new CommonResponse(message.getResponseText(), JSON.parseObject(message.getResponseText(), new TypeReference<CommonResponseEntity>() {
            }));
            if (commonResponse.getResponseEntity().getCode() >= 1000) {
                delivery.onMessageResponse(commonResponse);
            } else {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setErrorCode(12);
                errorResponse.setDescription(commonResponse.getResponseEntity().getMsg());
                errorResponse.setResponseText(message.getResponseText());
                //将已经解析好的 CommonResponseEntity 对象保存起来以便后面使用
                errorResponse.setReserved(responseEntity);
                //IResponseDispatcher内的一个方法，表示接收到错误消息，通过errorCode指定错误类型
                onSendMessageError(errorResponse, delivery);
            }
        } catch (JSONException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setResponseText(message.getResponseText());
            errorResponse.setErrorCode(11);
            errorResponse.setCause(e);
            onSendMessageError(errorResponse, delivery);
        }
    }
```
>onSendMessageError 方法后面会介绍

大概就是按照上面来实现，更详细的用法可以看[demo](https://github.com/0xZhangKe/WebSocketDemo/blob/master/app/src/main/java/com/zhangke/websocketdemo/AppResponseDispatcher.java)中是怎么做的。

### 配置统一的消息数据类型
一般来说，后台接口返回的数据是有个固定的格式的，通过上面的介绍我们已经了解到如何把数据转换成统一的类型发送到下游，下面我们先来简单的了解一下 [Response](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/Response.java)，我这里将所有后台返回的数据统一包装成一个 Response 对象，这是一个接口，你可以根据自己的需要来实现它：
```java
/**
 * WebSocket 响应数据接口
 * Created by ZhangKe on 2018/6/26.
 */
public interface Response<T> {

    /**
     * 获取响应的文本数据
     */
    String getResponseText();

    /**
     * 设置响应的文本数据
     */
    void setResponseText(String responseText);

    /**
     * 获取该数据的实体，可能为空，具体看实现类
     */
    T getResponseEntity();

    /**
     * 设置数据实体
     */
    void setResponseEntity(T responseEntity);
}
```
WebSocket 接收到数据后会首先包装成 [TextResponse](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/TextResponse.java) 对象发送出去，我们看一下 TextResponse 的代码：
```java
/**
 * 默认的消息响应事件包装类，
 * 只包含文本，不包含数据实体
 * Created by ZhangKe on 2018/6/27.
 */
public class TextResponse implements Response<String> {

    private String responseText;

    public TextResponse(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseEntity() {
        return null;
    }

    public void setResponseEntity(String responseEntity) {
    }
}
```
可以看到其中只包含了 String 类型的响应数据，没有对数据做其他操作，接收到什么就返回什么，其中的 responseText 表示 WebSocket 接收到的文本数据，除此之外我还提供了两个用于操作 ResponseEntity 的方法，我们可以将接收到的文本按照统一的格式转换成一个实体存入这个字段，然后再发送到下游。

比如后台接口的数据格式如下：
```json
{
    "message": "登陆成功",
    "data": {
        "name": "zhangke",
        "sex": "男",
        "nationality": "中国"
    },
    "code": 1000,
    "path": "app_user_login"
}
```
那么我们可以将数据转换成一个统一的泛型数据实体：
```java
/**
 * 后台接口返回的数据格式
 * Created by ZhangKe on 2018/6/27.
 */
public class CommonResponseEntity {

    private String message;
    private String data;
    private int code;
    private String path;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
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

data 字段中的数据交给对应模块解析，这里直接转成 String，然后包装成一个 CommonResponse 发送出去：
```java
public class CommonResponse implements Response<CommonResponseEntity> {

    private String responseText;
    private CommonResponseEntity responseEntity;

    public CommonResponse(String responseText, CommonResponseEntity responseEntity) {
        this.responseText = responseText;
        this.responseEntity = responseEntity;
    }

    @Override
    public String getResponseText() {
        return responseText;
    }

    @Override
    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    @Override
    public CommonResponseEntity getResponseEntity() {
        return this.responseEntity;
    }

    @Override
    public void setResponseEntity(CommonResponseEntity responseEntity) {
        this.responseEntity = responseEntity;
    }
}
```
### 错误信息的处理
刚刚已经介绍了如何统一处理消息及将消息转换成对应的实体，下面再说一下如何统一的处理错误信息。

所有的错误消息将统一包装成[ErrorResponse](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/ErrorResponse.java)对象发送出去，看一下其中的代码：
```java

/**
 * 出现错误时的响应
 * Created by ZhangKe on 2018/6/25.
 */
public class ErrorResponse {

    /**
     * 1-WebSocket 未连接或已断开
     * 2-WebSocketService 服务未绑定到当前 Activity/Fragment，或绑定失败
     * 3-WebSocket 初始化未完成
     * 11-数据获取成功，但是解析 JSON 失败
     * 12-数据获取成功，但是服务器返回数据中的code值不正确
     */
    private int errorCode;
    /**
     * 错误原因
     */
    private Throwable cause;
    /**
     * 发送的数据，可能为空
     */
    private String requestText;
    /**
     * 响应的数据，可能为空
     */
    private String responseText;
    /**
     * 错误描述，客户端可以通过这个字段来设置统一的错误提示等等
     */
    private String description;

    /**
     * 保留字段，可以自定义存放任意数据
     */
    private Object reserved;

    public ErrorResponse() {
    }

    /**
     * 1-WebSocket 未连接或已断开
     * 2-WebSocketService 服务未绑定到当前 Activity/Fragment，或绑定失败
     * 3-WebSocket 初始化未完成
     * 11-数据获取成功，但是解析 JSON 失败
     * 12-数据获取成功，但是服务器返回数据中的code值不正确
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 1-WebSocket 未连接或已断开
     * 2-WebSocketService 服务未绑定到当前 Activity/Fragment，或绑定失败
     * 3-WebSocket 初始化未完成
     * 11-数据获取成功，但是解析 JSON 失败
     * 12-数据获取成功，但是服务器返回数据中的code值不正确
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public String getRequestText() {
        return requestText;
    }

    public void setRequestText(String requestText) {
        this.requestText = requestText;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getReserved() {
        return reserved;
    }

    public void setReserved(Object reserved) {
        this.reserved = reserved;
    }
}
```
其中包括了五种错误类型，处理错误消息时就按照错误码来判断既可，另外还提供了一个 reserved 保留字段，这个用法可以看上面的**配置统一的消息处理器**那一节。
错误信息的处理同样也在 IResponseDispatcher 中处理，上面已经介绍了其中的 onMessageResponse ，现在再来说一下 onSendMessageError 方法：
```java
    /**
     * 统一处理错误信息，
     * 界面上可使用 ErrorResponse#getDescription() 来当做提示语
     */
    @Override
    public void onSendMessageError(ErrorResponse error, ResponseDelivery delivery) {
        switch (error.getErrorCode()) {
            case 1:
                error.setDescription("网络错误");
                break;
            case 2:
                error.setDescription("网络错误");
                break;
            case 3:
                error.setDescription("网络错误");
                break;
            case 11:
                error.setDescription("数据格式异常");
                Log.e(LOGTAG, "数据格式异常", error.getCause());
                break;
        }
        delivery.onSendMessageError(error);
    }
```
其实这里主要就是用来通过错误码给出不同的错误提示，其它的也没做什么，也可以在这里打印一下 Log 啊等等，code==12 时这里没有设置提示语，因为 12 表示接口已经请求成功了，但是后台后台接口给了错误的提示，比如密码错误等等，这时候错误信息应该是接口中给出，当然我们也可以自己来根据业务调整。

关于配置的就是这么多了，下面在介绍一下如何使用。
### 使用
我提供了一个 [AbsWebSocketActivity](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/AbsWebSocketActivity.java) 和一个 [AbsWebSocketFragment](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/AbsWebSocketFragment.java) 抽象基类，需要使用 WebSocket 的界面只需要继承这两个中的某一个就行，看一下 AbsWebSocketActivity 的代码：
```java
/**
 * 已经绑定了 WebSocketService 服务的 Activity，
 * <p>
 * Created by ZhangKe on 2018/6/25.
 */
public abstract class AbsWebSocketActivity extends AppCompatActivity implements IWebSocketPage {

    protected final String LOGTAG = this.getClass().getSimpleName();

    private WebSocketServiceConnectManager mConnectManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectManager = new WebSocketServiceConnectManager(this, this);
        mConnectManager.onCreate();
    }

    @Override
    public void sendText(String text) {
        mConnectManager.sendText(text);
    }

    /**
     * 服务绑定成功时的回调，可以在此初始化数据
     */
    @Override
    public void onServiceBindSuccess() {

    }

    /**
     * WebSocket 连接成功事件
     */
    @Override
    public void onConnected() {

    }

    /**
     * WebSocket 连接出错事件
     *
     * @param cause 出错原因
     */
    @Override
    public void onConnectError(Throwable cause) {

    }

    /**
     * WebSocket 连接断开事件
     */
    @Override
    public void onDisconnected() {

    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            mConnectManager.onDestroy();
        }
        super.onPause();
    }

}
```
代码很简洁的吧，有关于对 WebSocketService 的绑定、监听等操作全部放在了 [WebSocketServiceConnectManager](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/WebSocketServiceConnectManager.java) 类中，这样规避了代码重复问题，如果你想做一下自己的 BaseWebSocketActivity/BaseWebSocketFragment 直接按照这里面的代码实现既可。
AbsWebSocketActivity/AbsWebSocketFragment 中提供了一系列的方法以供使用，大部分方法一般都不需要用的，主要有三个方法要说一下：
```java
public void onServiceBindSuccess();//WebSocketService 服务绑定成功回调事件，可以在这个回调方法中初始化一下数据
public void onMessageResponse(Response message);//接收到消息回调事件
public void onSendMessageError(ErrorResponse error);//消息发送失败或接收到错误消息事件
```
onMessageResponse 及 onSendMessageError 方法中的 Response 和 ErrorResponse 参数上面已经介绍过了，另外还有一个 onServiceBindSuccess 方法，表示服务绑定成功，可以开始发送数据了。
### 重连机制
连接断开后会自动重连 20 次，每次间隔 500 毫秒。也可以通过监听网络连接变化自动重连，这部分我已经写好了，配置一下既可开启。
```java
WebSocketSetting.setReconnectWithNetworkChanged(true);
```
跟上面说的一样，这个也要在启动 WebSocketService 之前调用。

别忘了在 AndroidManifest.xml 配置广播和权限：
```xml
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <application>
        <!--省略代码-->
        <receiver android:name="com.zhangke.websocket.NetworkChangedReceiver" />
    </application>
```

好了关于如何配置及使用差不多就这样了，如果还有哪里不清楚的随时可以问我哦，下面在介绍的是其中的原理，不想看的可以直接跳过。

## 原理
关于原理我就大概的介绍一下，也没有太多的代码，细节部分我就不说了，先说一下设计。

在整个框架中的核心就是 [WebSocketThread](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/WebSocketThread.java) 线程，其内部采用的是消息驱动型的设计，使用 Looper.loop() 开启消息循环，其他模块将 WebSocket 的所有操作（消息发送、连接、断开等等）封装成消息的形式发送到该线程。

我们来看一下流程图：
![流程图](http://otp9vas7i.bkt.clouddn.com/websocketthread.png)

Service 在创建一个 WebSocketThread 对象后通过获取该线程的 Handler 来向其发送控制信息。
关于重连模块使用的是一个单独的类 [ReconnectManager](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/ReconnectManager.java) 来管理，其内部也持有一个 WebSocketThread 对象，当触发重连事件时通过 Handler 发送连接消息既可。
WebSocket 中的各种事件（连接成功、接收到消息等等）通过监听器 [SocketListener](https://github.com/0xZhangKe/WebSocketDemo/blob/master/websocketlib/src/main/java/com/zhangke/websocket/SocketListener.java) 通知 Service。

WebSocketThread 讲完了我在讲一下 WebSocketService ，也是比较重要，先看图：

![WebSocketService ](http://otp9vas7i.bkt.clouddn.com/websocketservice.jpg)

上图描述了 WebSocket 事件从 WebSocketThread 到 WebSocketService 再到 Activity/Fragment 的事件流向，WebSocketService 中通过一个 IResponseDispatcher 接口来分发事件，默认实现为 DefaultResponseDispatcher ，不做任何处理，直接发送到下游，也可以自己实现从而实现数据拦截、转换等操作。


好了就说到这里了，具体的一些细节直接看代码就行，还是很清晰的，要是有什么疑问直接问我也行。
我的微信：
![微信二维码](http://otp9vas7i.bkt.clouddn.com/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20180728142713.jpg)
