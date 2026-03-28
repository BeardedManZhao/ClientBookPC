package top.lingyuzhao.chat.client.pc;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DownloadUI {

    private static final Map<Integer, JProgressBar> bars = new HashMap<>();
    private static final Map<Integer, String> fileNameByBar = new HashMap<>();
    private static final JFrame frame = createFrame();
    private static final JPanel panel = new JPanel();
    private static final Pattern PATTERN = Pattern.compile("[/\\\\]+");

    static {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        frame.add(panel);
        // 设置拖拽和点击事件
        enableDrag();
    }

    private static JFrame createFrame() {
        JFrame f = new ModernFrame("下载列表");
        f.setSize(300, 200);
        f.setUndecorated(true);
        f.setAlwaysOnTop(true);

        // 右下角
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(d.width - 320, d.height - 240);

        // 初始不显示，等有下载任务时再显示
        f.setVisible(false);

        // 点击任意位置关闭
        f.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // 点击后隐藏或关闭
                frame.setVisible(false);
                // 或者 frame.dispose();
            }
        });
        return f;
    }

    // 在 createFrame() 方法中，或者在 static 代码块里添加此逻辑
    private static void enableDrag() {
        // 记录鼠标按下时的位置
        final int[] pointX = {0};
        final int[] pointY = {0};

        // 给整个 panel 添加鼠标监听（这样点击空白处也能拖拽）
        DownloadUI.panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                pointX[0] = e.getX();
                pointY[0] = e.getY();
            }
        });

        DownloadUI.panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                // 获取窗口当前左上角坐标
                int x = DownloadUI.frame.getLocation().x;
                int y = DownloadUI.frame.getLocation().y;

                // 移动窗口：新坐标 = 原坐标 + (当前鼠标位置 - 按下时鼠标位置)
                DownloadUI.frame.setLocation(x + e.getX() - pointX[0], y + e.getY() - pointY[0]);
            }
        });
    }

    /**
     * 这个方法可能会被后台线程调用，所以必须确保 Swing 操作在 EDT 中执行
     */
    public static void showOrUpdate(int id, String fullPath, int percent, boolean done) {
        SwingUtilities.invokeLater(() -> {
            // 1. 只要有任务，就确保窗口是显示的
            if (!frame.isVisible()) {
                frame.setVisible(true);
            }

            JProgressBar bar = bars.get(id);

            if (bar == null && !done) {
                // 新建进度条
                bar = new JProgressBar(0, 100);
                bar.setStringPainted(true);

                bars.put(id, bar);
                // 获取文件名
                final String[] split = PATTERN.split(fullPath);
                String name = split[split.length - 1];
                String shortName = name.length() > 20 ? name.substring(0, 20) + "... " : name + " ";
                fileNameByBar.put(id, shortName);

                panel.add(bar);
                panel.setBackground(new Color(0, 0, 0, 0));
                panel.revalidate(); // 重新布局
            } else if (bar != null) {
                // 更新现有进度条
                bar.setValue(percent);
                final String okName = fileNameByBar.get(id);
                if (okName != null) {
                    if (done) {
                        bar.setString("完成: " + okName);
                    } else {
                        bar.setString(okName + percent + '%');
                    }
                }
            }

            // 2. 清理逻辑
            if (done) {
                fileNameByBar.remove(id);
                bars.remove(id);
                if (bar != null) {
                    panel.remove(bar); // 从界面上移除组件
                    panel.revalidate();
                    panel.repaint();
                }
            }
        });
    }

    public static void dispose() {
        // 清理静态资源
        bars.clear();
        fileNameByBar.clear();
        panel.removeAll();
        frame.dispose();
    }




}