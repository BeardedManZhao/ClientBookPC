package top.lingyuzhao.chat.client.pc;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefRequestHandlerAdapter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class MainApp {

    static final String path;

    static {
        try {
            path = new File(
                    MainApp.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通用图标设置工具方法
     * 同时设置 JFrame 窗口图标和系统托盘图标
     *
     * @param frame    主窗口实例
     * @param iconPath 图标资源路径 (例如 "/favicon.ico")
     */
    public static void setAppIcons(JFrame frame, String iconPath) {
        Image iconImage;
        System.out.println("尝试加载图标！");
        // 1. 尝试加载图标
        try {
            // 使用 getResourceAsStream 安全地检查文件是否存在
            var inputStream = frame.getClass().getResourceAsStream(iconPath);

            if (inputStream == null) {
                System.out.println("提示：未找到图标 " + iconPath + "，跳过设置。");
                return; // 【关键点】文件不存在直接返回
            }

            // 读取图片
            iconImage = ImageIO.read(inputStream);

        } catch (IOException e) {
            e.printStackTrace(System.err);
            return; // 读取失败也返回
        }

        // --- 如果图标加载成功，执行以下两步 ---

        // 2. 设置 JFrame 窗口图标 (任务栏图标)
        if (iconImage != null) {
            frame.setIconImage(iconImage);
        } else {
            System.out.println("未找到图标文件！");
        }

        // 3. 设置系统托盘图标 (右下角)
        if (SystemTray.isSupported() && iconImage != null) {
            SystemTray tray = SystemTray.getSystemTray();

            // 创建托盘图标
            TrayIcon trayIcon = new TrayIcon(iconImage, frame.getTitle()); // 第二个参数是鼠标悬停提示
            trayIcon.setImageAutoSize(true); // 自动适应托盘大小

            // 添加双击托盘图标的监听器 (可选)
            trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // 双击托盘图标时，可以恢复窗口显示
                    if (e.getClickCount() == 2) {
                        frame.setVisible(true);
                        frame.setExtendedState(JFrame.NORMAL); // 取消最小化
                    }
                }
            });

            // 添加到系统托盘
            try {
                tray.add(trayIcon);
                System.out.println("托盘添加完毕！");
            } catch (AWTException e) {
                System.out.println("系统托盘添加失败");
                e.printStackTrace(System.err);
            }
        }
    }

    public static void main(String[] args) {
        // 1. 设置全局颜色 (在创建组件之前设置)
        // 前景色 = 进度条填充颜色
        UIManager.put("ProgressBar.foreground", new Color(0, 231, 223, 255));
        // 背景色 = 进度条背景颜色
        UIManager.put("ProgressBar.background", new Color(18, 20, 35, 255));
        SwingUtilities.invokeLater(() -> {
            CefAppBuilder builder = new CefAppBuilder();
            final CefSettings cefSettings = builder.getCefSettings();
            System.out.println("请稍等，我们正在为您初始化环境！");
            // 自动下载 + 配置环境
            cefSettings.windowless_rendering_enabled = false;
            // cookie 存储
            cefSettings.cache_path = path + "/cookie.save";
            final CefApp cefApp;
            try {
                cefApp = builder.build();
            } catch (IOException | InterruptedException | CefInitializationException | UnsupportedPlatformException e) {
                throw new RuntimeException(e);
            }

            CefClient client = cefApp.createClient();
            client.addDownloadHandler(new DownloadManager());
            client.addRequestHandler(new CefRequestHandlerAdapter() {
                @Override
                public boolean onOpenURLFromTab(
                        CefBrowser browser,
                        CefFrame frame,
                        String target_url,
                        boolean user_gesture) {

                    browser.loadURL(target_url);
                    return true;
                }
            });
            client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
                @Override
                public boolean onBeforePopup(
                        CefBrowser browser,
                        CefFrame frame,
                        String target_url,
                        String target_frame_name) {

                    browser.loadURL(target_url);
                    return true;
                }
            });
            CefBrowser browser = client.createBrowser(
                    "https://chat.lingyuzhao.top/chat.html",
                    false,
                    false
            );

            JFrame frame = new ModernFrame("CodeBook Chat");
            // 使用绝对路径（以/开头）从 classpath 根目录查找
            setAppIcons(frame, "/logo.jpg");
            frame.setSize(1200, 800);
            // 1. 设置为 DO_NOTHING_ON_CLOSE，防止窗口直接关闭但程序还在运行
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            frame.add(browser.getUIComponent(), BorderLayout.CENTER);

            // 2. 添加窗口监听器来处理关闭逻辑
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("正在安全关闭程序...");

                    // 1. 先关闭浏览器（给 JS 和渲染进程一点时间反应）
                    browser.close(false);

                    // 2. 释放客户端资源
                    client.dispose();

                    // 3. 释放 CEF 核心环境（这一步会保存 Cookie）
                    cefApp.dispose();

                    System.out.println("正在关闭 UI...");
                    // 4. 关闭 UI
                    frame.dispose(); // dispose 会自动移除组件，removeAll 不是必须的

                    // 如果 DownloadUI 是你自定义的一个静态 JFrame 或 JDialog
                    // 确保它里面有 dispose() 方法，或者它是当前 frame 的子窗口
                    try {
                        // 防止 DownloadUI 类不存在时报错（如果你还没写这个类）
                        DownloadUI.dispose();
                    } catch (Exception ex) {
                        // 忽略
                    }

                    // 5. 退出
                    try {
                        Thread.sleep(2000); // 等待 2 秒，这很重要！别删就对了
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.exit(0);
                }
            });

            frame.setVisible(true);
        });
    }
}