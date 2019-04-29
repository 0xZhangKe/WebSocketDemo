## 介绍
WebSocket 3.0 版本经过这段时间的开发终于完成并且通过测试，相比较于 2.0 版本有了很大的改动，程序的健壮性与扩展性有了很大的提高。

[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu) [![LICENSE](https://img.shields.io/badge/license-NPL%20(The%20996%20Prohibited%20License)-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE) [![HitCount](http://hits.dwyl.io/0xZhangKe/WebSocketDemo.svg)](http://hits.dwyl.io/0xZhangKe/WebSocketDemo)

实际上，以前的版本很大程度上都是为了应付公司业务而做的，顺手开源出来，但我发现随着使用者越来越多，问题也逐渐凸显出来，再加上经过前段时间的学习技术上有了长足的进步，就想着把这个给重构一遍。
其实做这个技术上并没有什么技术上的难点，但是要做开源，要给别人用，就会有很大的挑战。不仅要考虑到程序的健壮性，还要考虑如何用最简单的方式，给用户提供更多的功能，并且兼顾到可扩展性。
我之前花了很长时间研究过设计模式相关的东西，也读了一些框架的源码，所以我的技术也一直更偏向于架构设计方向，这个框架中也用到了很多设计模式相关知识点。
3.0 版本的开发时间也不过一个月左右，但实际上我在开发之前就花了很久考虑如何设计架构，因为核心实现方式变了，所以几乎一切都是从零开始。最终选定了现在的方案，也是现阶段我能想到的最佳方案。

## 3.0 版本的改动
最主要的变动是核心实现方式从 Service 变更为独立线程，解决了新版本 Android 系统启动 Service 的问题以及可以准确控制连接的启动与断开。
所以因为核心方式变了也就没有 BaseWebSocketActivity 以及相关概念，所有对 WebSocket 相关的操作都是通过 [WebSocketHandler](https://github.com/0xZhangKe/WebSocketDemo/blob/3.0/websocketlib/src/main/java/com/zhangke/websocket/WebSocketHandler.java) 来实现的。
现在 WebSocketHandler 是个很重要的概念，我们无论是 WebSocket 的初始化、创建连接、断开连接、数据收发等等都要使用它来实现，其中具体的方法列表[点此查看文档](https://github.com/0xZhangKe/WebSocketDemo/tree/3.0/doc)。

## 如何集成
这一点与以前一样，也有两种使用方式。

### Gradle 方式集成
在对应 model 的 build.gradle 中添加依赖：
```
implementation 'com.github.0xZhangKe:WebSocketDemo:3.0'
```
然后 sync 一下，如果出现类似的错误：

Failed to resolve: com.github.0xZhangKe:WebSocketDemo:3.0

那意味着你还没添加 Github 的仓库，到项目根目录中的 build.gradle 中添加如下代码：
```
maven { url = 'https://jitpack.io' }
```

### 第二种集成方式
这个就很简单了，直接把 websocketlib 中的代码拷贝到自己的项目中就行，具体怎么做就看你的个人喜好。

## 开始使用
此时你已经把框架集成到项目中了，再经过简单的几步配置即可使用。
首先，最基本的，我们要配置 WebSocket 连接地址，要说明的是，关于 WebSocket 的相关配置都在 [WebSocketSetting](https://github.com/0xZhangKe/WebSocketDemo/blob/3.0/websocketlib/src/main/java/com/zhangke/websocket/WebSocketSetting.java) 中。
我们通过如下的代码设置连接地址：
```java
WebSocketSetting setting = new WebSocketSetting();
//连接地址，必填，例如 wss://localhost:8080
setting.setConnectUrl("your connect url");
```
除了连接地址之外，WebSocketSetting 中还提供了很多相关配置，我挑几个重要的说一下。
```java
//设置连接超时时间
setting.setConnectTimeout(60);

//设置断开后的重连次数，可以设置的很大，不会有什么性能上的影响
setting.setReconnectFrequency(40);

//设置 Headers
setting.setHttpHeaders(header);

//设置消息分发器，接收到数据后先进入该类中处理，处理完再发送到下游
setting.setResponseProcessDispatcher(new AppResponseDispatcher());
//接收到数据后是否放入子线程处理，只有设置了 ResponseProcessDispatcher 才有意义
setting.setProcessDataOnBackground(true);

//网络状态发生变化后是否重连，
//需要调用 WebSocketHandler.registerNetworkChangedReceiver(context) 方法注册网络监听广播
setting.setReconnectWithNetworkChanged(true);
```
上面基本上包含了我们常用的一些配置了，详细介绍可[查看文档](https://github.com/0xZhangKe/WebSocketDemo/tree/3.0/doc)，或者直接问我。

设置好之后就可直接开始连接啦，上面说过连接使用 WebSocketHandler 来操作，具体如下：
```java
//通过 init 方法初始化默认的 WebSocketManager 对象
WebSocketManager manager = WebSocketHandler.init(setting);
//启动连接
manager.start();
```
我们对 WebSocket 的连接管理、数据收发，本质上是使用 [WebSocketManager](https://github.com/0xZhangKe/WebSocketDemo/blob/3.0/websocketlib/src/main/java/com/zhangke/websocket/WebSocketManager.java) 来实现。
上面的 WebSocketHandler.init(setting) 方法也是为了获取一个默认的 WebSocketManager 对象。