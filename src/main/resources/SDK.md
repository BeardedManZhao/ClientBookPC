# CodeBookChat-Java SDK 说明

以下示例演示：

* 登录
* 获取用户信息
* 建立 WebSocket 连接
* 注册命令处理器
* 发送命令
* 关闭连接

```java
import com.alibaba.fastjson2.JSONObject;
import okhttp3.Response;
import top.lingyuzhao.chat.client.api.*;
import top.lingyuzhao.chat.client.data.User;

import java.io.IOException;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 开始登录
        final CodeBookChatApi api = CodeBookChatApi.login(10003, "123123", "https://chat.lingyuzhao.top", "wss://chat.lingyuzhao.top", new WebSocketClientListener() {
            @Override
            public void onOpen(WebSocketClient client, Response response) {
                /* 连接建立成功之后的调用 */
            }

            @Override
            public void onClosing(WebSocketClient client, int code, String reason) {
                /* 连接关闭时的调用 */
            }

            @Override
            public void onClosed(WebSocketClient client, int code, String reason) {
                /* 连接关闭之后的调用 */
            }

            @Override
            public void onFailure(WebSocketClient client, Throwable t, Response response) {
                /* 连接发生错误时的调用 */
            }
        });
        // 获取到自己的身份
        final User self = api.getSelf();
        System.out.println(JSONObject.from(self));
        // 获取到自己的所有好友
        api.getFriends().forEach((aLong, friend) -> System.out.println("好友：" + JSONObject.from(friend)));
        // 获取到某个用户的信息
        User user = api.getUser(10004);
        System.out.println(JSONObject.from(user));
        // 判断 10004 是否在线
        System.out.println(api.isOnline(10004));
        // 判断 10001 是否在线
        System.out.println(api.isOnline(10001));
        // 获取到 手机APP 下载链接
        System.out.println(api.getDownloadAppLink());
        // 获取到当前使用的 token
        System.out.println(api.getToken());
        // 获取到token的获取时间
        System.out.println(api.getLastGetTokenTimeMs());
        // 获取到 手机APP 版本信息
        System.out.println(Arrays.toString(api.getAppVersion()));

        // 获取到 ws 操作API
        try (final WsCommandApi wsCommandApi = api.getWsCommandApi()) {
            wsCommandApi.playRing(); // 播放铃声
            wsCommandApi.setReconnectDelay(1000); // 设置重连间隔
            // 设置 0 号命令监听器 服务器返回的 command=0 的数据会进入到这个函数
            wsCommandApi.setHandler(0, (jsonObject, client) -> System.out.println("0接收到：" + jsonObject));
            // 设置 4 号命令监听器 服务器返回的 command=4 的数据会进入到这个函数
            wsCommandApi.setHandler(4, (jsonObject, client) -> System.out.println("4接收到：" + jsonObject));
            //  TODO 危险操作 设置 ping超时处理函数
            wsCommandApi.setPongTimeoutHandler(new PongTimeoutHandler(api) {
                @Override
                public void accept(WsCommandApi wsCommandApi) {
                    // 在上面可以写其它部分的逻辑
                    System.out.println("长时间没收到服务器的 pong，因此尝试重连");
                    // 因为这里不建议您删除，库内部实现好了一些逻辑，一旦删除可能将导致重连机制受损
                    super.accept(wsCommandApi);
                    // 在下面也可以写其它逻辑
                    System.out.println("pong 超时函数处理结束！");
                }
            });
            // 阻塞 模拟运行中
            for (int i = 0; i < 3; i++) {
                System.out.println("发送命令：4");
                // 发送命令
                wsCommandApi.sendMessage(4);
                // 模拟运行中的情况，等待接收回复数据
                Thread.sleep(1000);
            }
            // 发送命令 0 给 1 号用户。0发送的是消息 需要有数据体
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sendId", user.getUserId());
            jsonObject.put("recId", 10001);
            jsonObject.put("html", "你好");
            jsonObject.put("ts", System.currentTimeMillis());
            System.out.println("发送命令 0");
            wsCommandApi.sendMessage(0, jsonObject);
            // 模拟运行中的情况，等待接收回复数据
            Thread.sleep(1000);
        }
    }
}
```

---

## 示例运行结果

```
{"avatarLink":"/image/logo.jpg","desc":"我是测试用的号","lastOfflineTimeMs":1774051935933,"online":false,"userId":10003,"userName":"测试大师"}
好友：{"haveNoRead":true,"recId":1,"recTimeMs":1772526155807,"sendTimeMs":1772526154807}
好友：{"haveNoRead":false,"recId":10001,"recTimeMs":1772526269624,"sendTimeMs":1772526203667}
好友：{"haveNoRead":false,"recId":2,"recTimeMs":1773364692951,"sendTimeMs":1773364691951}
好友：{"haveNoRead":false,"recId":10006,"recTimeMs":1773284921479,"sendTimeMs":1773284898218}
好友：{"haveNoRead":false,"recId":10009,"recTimeMs":0,"sendTimeMs":1773284855005}
{"avatarLink":"/files/10001/10004-avatar.jpeg","desc":"这是我蛋仔名啊。名字！","lastOfflineTimeMs":1774052248675,"online":false,"userId":10004,"userName":"阿狸蛋壳"}
false
true
https://chat.lingyuzhao.top/app/download
keKWbEelSy
1774055107410
[1.9, 1.优化编译版本
2.减小安装包体积
3.具有更快的速度
4.修复视频无法全屏播放的问题
5.修复视频点击下载按钮后无反应的问题
PS：有些老型号手机可能不支持视频下载]
发送命令：4
4接收到：{"command":4,"count":1}
4接收到：{"command":4,"count":1}
发送命令：4
4接收到：{"command":4,"count":1}
发送命令：4
4接收到：{"command":4,"count":1}
发送命令 0
0接收到：{"sendId":10003,"recId":10001,"html":"你好","ts":1774055108537,"command":0}
```