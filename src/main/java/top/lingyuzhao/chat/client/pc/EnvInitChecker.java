package top.lingyuzhao.chat.client.pc;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EnvInitChecker implements AutoCloseable {

    private final GlobalMessageBox messageBox;
    private final ScheduledExecutorService executor;
    public static float progress = -1;

    private final String[] messages = {
            "请稍等，我们正在为您初始化环境！这需要网络哦",
            "感谢您下载我们的软件",
            "本软件为开源版本，欢迎各位提意见",
            "你知道吗？软件的目录可以直接被移动哦！",
            "稍后可以注册或登录自己的账户"
    };

    private int index = 0;
    private ScheduledFuture<?> future;

    public EnvInitChecker(GlobalMessageBox messageBox) {
        this.messageBox = messageBox;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 开始检测
     */
    public void start(String path) {

        File file = new File(path, "jcef-bundle");

        // ⭐ 先强制显示一次（关键，避免“第一次不显示”问题）
        messageBox.showMessage(messages[0]);

        future = executor.scheduleAtFixedRate(() -> {

            // 1️⃣ 检测文件
            if (file.exists()) {
                System.out.println("已检测到环境，无需更新!");
                messageBox.closeMessageNoLambda();
                messageBox.showMessageNoLambda("环境检测结束");
                stop();

                messageBox.closeMessageNoLambda();
                return;
            }

            // 2️⃣ 轮播提示
            String msg = "【初始化中... " + (progress > 0 ? Math.max(progress / 100, 99) + "%】" : "】") + messages[index++ % messages.length];
            System.out.println("检测中... " + msg);
            messageBox.closeMessageNoLambda();
            messageBox.showMessageNoLambda(msg);

        }, 0, 15, TimeUnit.SECONDS);
    }

    /**
     * 停止检测
     */
    public void stop() {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        stop();
        executor.shutdownNow();
    }
}