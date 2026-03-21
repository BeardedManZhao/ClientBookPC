package top.lingyuzhao.chat.client.pc;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ModernFrame —— 深色玻璃拟态风格的 JFrame 替代品。
 * <p>
 * 用法与普通 JFrame 完全一致：
 * <pre>
 *   ModernFrame frame = new ModernFrame("我的应用");
 *   frame.setSize(900, 600);
 *   frame.setLayout(new BorderLayout());
 *   frame.add(myPanel, BorderLayout.CENTER);
 *   frame.setVisible(true);
 * </pre>
 * <ul>
 *   <li>自定义标题栏（macOS 风格控制按钮）</li>
 *   <li>可拖拽 / 全方位可拉伸缩放</li>
 *   <li>圆角 + 透明背景 + 玻璃渐变叠层</li>
 *   <li>双击标题栏最大化 / 还原</li>
 * </ul>
 */
public class ModernFrame extends JFrame {

    // ── 调色板 ────────────────────────────────────────────────
    /**
     * 主背景：深蓝黑，带适度不透明度
     */
    private static final Color BG_BASE = new Color(10, 12, 22, 235);
    /**
     * 玻璃高光顶层渐变起点
     */
    private static final Color GLASS_TOP = new Color(255, 255, 255, 22);
    /**
     * 玻璃高光顶层渐变终点
     */
    private static final Color GLASS_BOTTOM = new Color(255, 255, 255, 0);
    /**
     * 边框高光
     */
    private static final Color BORDER_OUTER = new Color(255, 255, 255, 45);
    /**
     * 内嵌阴影线（内边框）
     */
    private static final Color BORDER_INNER = new Color(255, 255, 255, 8);
    /**
     * 标题栏底层色
     */
    private static final Color TITLEBAR_BG = new Color(18, 20, 35, 210);
    /**
     * 标题栏分隔线
     */
    private static final Color TITLEBAR_SEP = new Color(255, 255, 255, 18);
    /**
     * 主文字
     */
    private static final Color TEXT_PRIMARY = new Color(210, 215, 235);
    /**
     * 次要文字
     */
    private static final Color TEXT_MUTED = new Color(120, 130, 160);
    /**
     * 强调色（蓝紫）
     */
    private static final Color ACCENT = new Color(110, 148, 255);

    // macOS 红绿黄控制按钮
    private static final Color BTN_CLOSE = new Color(255, 96, 86);
    private static final Color BTN_MIN = new Color(255, 191, 47);
    private static final Color BTN_MAX = new Color(40, 202, 65);
    private static final Color BTN_HOVER_FG = new Color(0, 0, 0, 160);

    // ── 布局常量 ──────────────────────────────────────────────
    private static final int CORNER_R = 14;   // 圆角半径
    private static final int TITLE_H = 40;   // 标题栏高度
    private static final int RESIZE_BORDER = 7;    // 可拖拽缩放边缘厚度
    private static final int MIN_W = 400;
    private static final int MIN_H = 280;

    // ── 字体 ─────────────────────────────────────────────────
    /**
     * 选择系统中最合适的 UI 字体 (显式兼容中文)
     */
    private static final Font FONT_TITLE;

    static {
        Font f = null;
        // 定义一个优先级列表：Windows -> Mac -> Linux/通用
        String[] candidates = {"Microsoft YaHei", "PingFang SC", "SimHei", "WenQuanYi Micro Hei"};

        for (String fontName : candidates) {
            Font temp = new Font(fontName, Font.PLAIN, 13);
            // 如果创建出来的字体家族名和我们想要的一样，说明系统里有这个字体
            if (temp.getFamily().equalsIgnoreCase(fontName)) {
                f = temp;
                break;
            }
        }

        // 兜底：如果上面都没找到，就用 Java 的通用无衬线字体（通常也能显示中文）
        if (f == null) {
            f = new Font("SansSerif", Font.PLAIN, 13);
        }

        FONT_TITLE = f;
    }

    // ── 状态 ─────────────────────────────────────────────────
    /**
     * 用户实际往里 add() 内容的容器
     */
    private JPanel contentWrapper;
    private JLabel titleLabel;

    // 拖拽移动
    private Point dragOffset;
    // 拖拽缩放
    private boolean isResizing;
    private int resizeCursor;
    private Rectangle resizeStartBounds;
    private Point resizeStartScreen;

    // ── 构造 ─────────────────────────────────────────────────
    public ModernFrame(String title) {
        setTitle(title);
        setUndecorated(true);
        // 让窗口背景完全透明，由我们自己绘制圆角玻璃
        setBackground(new Color(0, 0, 0, 0));
        buildUI(title);
        buildResizeHandler();

        // 窗口 resize 时同步更新系统裁剪形状
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                syncShape();
            }
        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(920, 640);
        setLocationRelativeTo(null);
    }

    // ── 公开 API（对外表现与普通 JFrame 一致） ───────────────

    private static Graphics2D prepare(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        return g2;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 启用硬件加速
            System.setProperty("sun.java2d.opengl", "true");

            ModernFrame frame = new ModernFrame("ModernFrame · 玻璃拟态演示");
            frame.setSize(820, 520);
            frame.setLocationRelativeTo(null);

            // 内容区演示
            JPanel demo = new JPanel(new GridBagLayout());
            demo.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.gridx = 0;
            gbc.gridy = 0;

            JLabel hello = new JLabel("Glass Morphism · 深色玻璃风格");
            hello.setForeground(new Color(210, 215, 235));
            hello.setFont(new Font("Segoe UI", Font.BOLD, 22));
            demo.add(hello, gbc);

            gbc.gridy = 1;
            JLabel sub = new JLabel("像普通 JFrame 一样使用，外观全部由 ModernFrame 接管。");
            sub.setForeground(new Color(120, 130, 160));
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            demo.add(sub, gbc);

            gbc.gridy = 2;
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(68);
            bar.setStringPainted(true);
            bar.setString("下载进度  68%");
            bar.setPreferredSize(new Dimension(400, 22));
            bar.setForeground(ACCENT);
            bar.setBackground(new Color(255, 255, 255, 18));
            bar.setBorderPainted(false);
            demo.add(bar, gbc);

            frame.add(demo); // 与普通 JFrame 用法相同
            frame.setVisible(true);
        });
    }

    // ── 内部构建 ─────────────────────────────────────────────

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        if (titleLabel != null) titleLabel.setText(title);
    }

    /**
     * 重写 getContentPane，使 frame.add() / frame.setLayout() 等操作
     * 直接作用于标题栏下方的内容区域。
     */
    @Override
    public Container getContentPane() {
        return contentWrapper != null ? contentWrapper : super.getContentPane();
    }

    private void buildUI(String title) {
        // 根面板：负责绘制玻璃背景
        RootGlassPanel root = new RootGlassPanel();
        root.setLayout(new BorderLayout(0, 0));

        // 标题栏
        JPanel titleBar = buildTitleBar(title);
        root.add(titleBar, BorderLayout.NORTH);

        // 内容区容器（透明，用户在这里 add 组件）
        contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        // 内容区与边缘留 1px 空间，让边框线可见
        contentWrapper.setBorder(new EmptyBorder(0, 1, 1, 1));
        root.add(contentWrapper, BorderLayout.CENTER);

        // 替换 JFrame 默认 contentPane
        super.setContentPane(root);
    }

    private JPanel buildTitleBar(String title) {
        JPanel bar = new TitleBarPanel();
        bar.setLayout(new BorderLayout(0, 0));
        bar.setPreferredSize(new Dimension(0, TITLE_H));
        bar.setBorder(new EmptyBorder(0, 14, 0, 14));

        // ── 左侧：macOS 三色控制按钮 ──────────
        // 用 BoxLayout + glue 实现真正的垂直居中
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        controls.setOpaque(false);
        // 左侧内边距
        controls.add(Box.createHorizontalStrut(2));

        ControlButton closeBtn = new ControlButton(BTN_CLOSE, "x");
        ControlButton minBtn = new ControlButton(BTN_MIN, "−");
        ControlButton maxBtn = new ControlButton(BTN_MAX, "+");

        // ✅ 派发 WINDOW_CLOSING 事件，确保 WindowListener.windowClosing() 被正确回调
        closeBtn.addActionListener(e ->
                dispatchEvent(new WindowEvent(ModernFrame.this, WindowEvent.WINDOW_CLOSING)));
        minBtn.addActionListener(e -> setState(Frame.ICONIFIED));
        maxBtn.addActionListener(e -> {
            if (getExtendedState() == Frame.MAXIMIZED_BOTH) setExtendedState(Frame.NORMAL);
            else setExtendedState(Frame.MAXIMIZED_BOTH);
        });

        // 每个按钮都强制固定最大尺寸，防止 BoxLayout 拉伸
        for (ControlButton btn : new ControlButton[]{closeBtn, minBtn, maxBtn}) {
            btn.setAlignmentY(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(btn.getPreferredSize());
        }
        controls.add(closeBtn);
        controls.add(Box.createHorizontalStrut(7));
        controls.add(minBtn);
        controls.add(Box.createHorizontalStrut(7));
        controls.add(maxBtn);
        bar.add(controls, BorderLayout.WEST);

        // ── 中央：标题 ───────────────────────
        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(FONT_TITLE);
        bar.add(titleLabel, BorderLayout.CENTER);

        // ── 右侧：留白（与左侧等宽，保证标题居中） ─
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        // 宽度与左侧 controls 对称：2(strut) + 3×13(btn) + 2×7(gap) = 55
        spacer.setPreferredSize(new Dimension(55, 0));
        bar.add(spacer, BorderLayout.EAST);

        // ── 拖拽移动 ──────────────────────────
        MouseAdapter drag = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e))
                    dragOffset = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset == null || getExtendedState() == Frame.MAXIMIZED_BOTH) return;
                Point screen = e.getLocationOnScreen();
                setLocation(screen.x - dragOffset.x, screen.y - dragOffset.y);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragOffset = null;
            }

            // 双击最大化 / 还原
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (getExtendedState() == Frame.MAXIMIZED_BOTH) setExtendedState(Frame.NORMAL);
                    else setExtendedState(Frame.MAXIMIZED_BOTH);
                }
            }
        };
        bar.addMouseListener(drag);
        bar.addMouseMotionListener(drag);
        // 标题 label 也需要透传拖拽（否则点到文字拖不动）
        titleLabel.addMouseListener(drag);
        titleLabel.addMouseMotionListener(drag);

        return bar;
    }

    private void buildResizeHandler() {
        MouseAdapter ra = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int dir = hitTestResize(e.getPoint());
                setCursor(dir == -1 ? Cursor.getDefaultCursor()
                        : Cursor.getPredefinedCursor(dir));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int dir = hitTestResize(e.getPoint());
                if (dir == -1) return;
                isResizing = true;
                resizeCursor = dir;
                resizeStartBounds = getBounds();
                resizeStartScreen = e.getLocationOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isResizing) return;
                applyResize(e.getLocationOnScreen());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isResizing = false;
            }
        };
        addMouseListener(ra);
        addMouseMotionListener(ra);
    }

    private int hitTestResize(Point p) {
        int w = getWidth(), h = getHeight(), b = RESIZE_BORDER;
        boolean l = p.x < b, r = p.x > w - b, t = p.y < b, bo = p.y > h - b;
        if (l && t) return Cursor.NW_RESIZE_CURSOR;
        if (r && t) return Cursor.NE_RESIZE_CURSOR;
        if (l && bo) return Cursor.SW_RESIZE_CURSOR;
        if (r && bo) return Cursor.SE_RESIZE_CURSOR;
        if (l) return Cursor.W_RESIZE_CURSOR;
        if (r) return Cursor.E_RESIZE_CURSOR;
        if (t) return Cursor.N_RESIZE_CURSOR;
        if (bo) return Cursor.S_RESIZE_CURSOR;
        return -1;
    }

    // ── 内部组件 ─────────────────────────────────────────────

    private void applyResize(Point screen) {
        int dx = screen.x - resizeStartScreen.x;
        int dy = screen.y - resizeStartScreen.y;
        Rectangle b = new Rectangle(resizeStartBounds);
        switch (resizeCursor) {
            case Cursor.SE_RESIZE_CURSOR:
                b.setSize(Math.max(MIN_W, b.width + dx), Math.max(MIN_H, b.height + dy));
                break;
            case Cursor.SW_RESIZE_CURSOR:
                b.setLocation(resizeStartBounds.x + dx, b.y);
                b.setSize(Math.max(MIN_W, b.width - dx), Math.max(MIN_H, b.height + dy));
                break;
            case Cursor.NE_RESIZE_CURSOR:
                b.setLocation(b.x, resizeStartBounds.y + dy);
                b.setSize(Math.max(MIN_W, b.width + dx), Math.max(MIN_H, b.height - dy));
                break;
            case Cursor.NW_RESIZE_CURSOR:
                b.setLocation(resizeStartBounds.x + dx, resizeStartBounds.y + dy);
                b.setSize(Math.max(MIN_W, b.width - dx), Math.max(MIN_H, b.height - dy));
                break;
            case Cursor.E_RESIZE_CURSOR:
                b.setSize(Math.max(MIN_W, b.width + dx), b.height);
                break;
            case Cursor.W_RESIZE_CURSOR:
                b.setLocation(resizeStartBounds.x + dx, b.y);
                b.setSize(Math.max(MIN_W, b.width - dx), b.height);
                break;
            case Cursor.S_RESIZE_CURSOR:
                b.setSize(b.width, Math.max(MIN_H, b.height + dy));
                break;
            case Cursor.N_RESIZE_CURSOR:
                b.setLocation(b.x, resizeStartBounds.y + dy);
                b.setSize(b.width, Math.max(MIN_H, b.height - dy));
                break;
        }
        setBounds(b);
    }

    /**
     * 同步系统窗口裁剪形状（用于鼠标事件的圆角剔除）
     */
    private void syncShape() {
        try {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(),
                    CORNER_R * 2, CORNER_R * 2));
        } catch (UnsupportedOperationException ignored) {
            // 少数 Linux WM 不支持，忽略即可
        }
    }

    /**
     * macOS 风格圆形控制按钮。
     * 平时只显示颜色圆点，鼠标悬浮时显示功能图标。
     */
    private static class ControlButton extends JButton {
        private final Color base;
        private final String glyph;
        private boolean hovered;

        ControlButton(Color base, String glyph) {
            super();
            this.base = base;
            this.glyph = glyph;
            setPreferredSize(new Dimension(13, 13));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        private static Graphics2D prepare(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            return g2;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = prepare(g);
            int w = getWidth(), h = getHeight();

            // 圆形背景
            g2.setColor(getModel().isPressed() ? base.darker() : base);
            g2.fillOval(0, 0, w, h);

            // 悬浮时绘制图标
            if (hovered) {
                g2.setColor(BTN_HOVER_FG);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(glyph)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(glyph, tx, ty);
            }
            g2.dispose();
        }
    }

    // ── 工具方法 ─────────────────────────────────────────────

    /**
     * 根玻璃面板：绘制深色玻璃背景、渐变高光、内外边框。
     */
    private static class RootGlassPanel extends JPanel {
        RootGlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = prepare(g);
            int w = getWidth(), h = getHeight();
            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, CORNER_R * 2, CORNER_R * 2);

            // 1. 主背景填充
            g2.setColor(BG_BASE);
            g2.fill(shape);

            // 2. 玻璃高光渐变（顶部 40% 区域）
            GradientPaint gloss = new GradientPaint(0, 0, GLASS_TOP, 0, h * 0.40f, GLASS_BOTTOM);
            g2.setPaint(gloss);
            g2.fill(shape);

            // 3. 外边框高光
            g2.setPaint(BORDER_OUTER);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, CORNER_R * 2, CORNER_R * 2));

            // 4. 内嵌高光线（让边框有厚度感）
            g2.setPaint(BORDER_INNER);
            g2.draw(new RoundRectangle2D.Double(1.5, 1.5, w - 3, h - 3, CORNER_R * 2 - 2, CORNER_R * 2 - 2));

            g2.dispose();
        }
    }

    // ── 快速预览 Demo ────────────────────────────────────────

    /**
     * 标题栏面板：绘制半透明深色背景 + 底部分隔线。
     * 顶部圆角与根面板一致，底部直角（被下方内容覆盖）。
     */
    private static class TitleBarPanel extends JPanel {
        TitleBarPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = prepare(g);
            int w = getWidth(), h = getHeight();

            // 顶部圆角，底部方形（多画 CORNER_R 像素使底部不显示圆角）
            g2.setColor(TITLEBAR_BG);
            g2.fillRoundRect(0, 0, w, h + CORNER_R, CORNER_R * 2, CORNER_R * 2);

            // 底部分隔线
            g2.setColor(TITLEBAR_SEP);
            g2.drawLine(1, h - 1, w - 2, h - 1);

            g2.dispose();
        }
    }
}