package top.lingyuzhao.chat.client.pc;

import javax.swing.*;
import java.awt.*;

public class GlobalMessageBox implements AutoCloseable {

    private final JFrame frame;
    private JLabel messageLabel;

    public GlobalMessageBox(JFrame frame) {
        this.frame = frame;
        init();
    }

    private void init() {
        // 清空原有内容（你如果不想清空可以删掉这行）
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageLabel.setForeground(Color.WHITE);

        frame.add(messageLabel, BorderLayout.CENTER);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        frame.revalidate();
        frame.repaint();
    }

    /**
     * 显示消息（直接用传入的 frame）
     */
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> this.showMessageNoLambda(message));
    }

    /**
     * 隐藏窗口
     */
    public void closeMessage() {
        SwingUtilities.invokeLater(this::closeMessageNoLambda);
    }

    /**
     * 显示消息（直接用传入的 frame）
     */
    public void showMessageNoLambda(String message) {
        // 1. 更新数据
        messageLabel.setText(message);
        // 2. 强制刷新 UI
        messageLabel.revalidate();
        messageLabel.repaint(); // 只重绘像素，不触碰布局
        // 3. 如果窗口不可见，则显示
        if (!frame.isVisible()) {
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }

    /**
     * 隐藏窗口
     */
    public void closeMessageNoLambda() {
        frame.setVisible(false);
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(false);
            frame.dispose();
        });
    }
}