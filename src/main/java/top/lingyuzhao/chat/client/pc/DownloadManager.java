package top.lingyuzhao.chat.client.pc;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.callback.CefDownloadItemCallback;
import org.cef.handler.CefDownloadHandlerAdapter;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadManager extends CefDownloadHandlerAdapter {

    private final Map<String, Integer> pathToId = new ConcurrentHashMap<>();
    // ✅ 新增：记录每个 ID 当前最新的 path（用于反查）
    private final Map<Integer, String> idToPath = new ConcurrentHashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(0);
    public DownloadManager() {
        // 不需要加载配置了，CEF 会自动记住上次下载路径
        System.out.println("[Download] 使用原生 Win11 下载对话框，自动记忆路径。");
    }

    @Override
    public boolean onBeforeDownload(CefBrowser browser,
                                    CefDownloadItem item,
                                    String suggestedName,
                                    CefBeforeDownloadCallback callback) {

        // 【关键修改】返回 false
        // 含义：不拦截，让 CEF 使用原生的“另存为”对话框。
        // 好处：
        // 1. 界面是标准的 Windows 11 风格（漂亮、流畅）。
        // 2. 自动记忆上次下载的文件夹（无需手动保存 properties）。
        // 3. 不会出现“弹两次”的 Bug。
        // 4. 避免了 Swing JFileChooser 导致的死锁或 Comparator 错误。
        // ⭐ 真正开始下载
        callback.Continue(suggestedName, true);
        return true;
    }

    @Override
    public void onDownloadUpdated(CefBrowser browser,
                                  CefDownloadItem item,
                                  CefDownloadItemCallback callback) {
        final String fullPath = item.getFullPath();
        if (fullPath == null || fullPath.isEmpty()) return;

        // ✅ 核心：用 CEF 原始 ID（哪怕是-1）+ 首次路径 联合兜底
        // 实际上我们用"首次注册的路径"作为锚点
        int myId = pathToId.computeIfAbsent(fullPath, k -> idGen.incrementAndGet());

        // ✅ 检测路径是否发生了变化（重名文件改名场景）
        String trackedPath = idToPath.get(myId);
        if (trackedPath == null) {
            // 首次见到这个 ID，记录路径
            idToPath.put(myId, fullPath);
        } else if (!trackedPath.equals(fullPath)) {
            // 路径变了（file.zip → file (1).zip）
            // 把旧 key 迁移到新 path
            pathToId.remove(trackedPath);
            pathToId.put(fullPath, myId);
            idToPath.put(myId, fullPath);
        }

        int percent = Math.max(0, item.getPercentComplete());
        boolean isDone = item.isComplete() || item.isCanceled();

        SwingUtilities.invokeLater(() -> DownloadUI.showOrUpdate(
                myId, fullPath, percent, isDone
        ));

        if (isDone) {
            pathToId.remove(fullPath);
            idToPath.remove(myId);
        }
    }
}