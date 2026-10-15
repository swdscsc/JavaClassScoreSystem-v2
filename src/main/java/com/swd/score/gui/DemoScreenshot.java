package com.swd.score.gui;

import com.swd.score.model.User;
import com.swd.score.service.ScoreService;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * 演示截图工具（仅开发用）：
 * 自动登录 → 打开各界面 → Robot 抓屏保存 PNG → 退出。
 * 用法: java com.swd.score.Main --demo-shot <输出目录>
 */
public final class DemoScreenshot {

    private DemoScreenshot() {}

    public static void run(String outDir) throws Exception {
        Path out = Path.of(outDir);
        Files.createDirectories(out);

        // 1) 登录界面
        LoginFrame login = show(() -> new LoginFrame(), 160, 140);
        shot(login, out.resolve("01-登录界面.png"));

        // 2) 教师端主界面（程序化登录，绕过手工输入）
        ScoreService svc = new ScoreService();
        User teacher = svc.login("teacher", "123456");
        if (teacher == null) throw new IllegalStateException("teacher 登录失败（检查 MySQL 与 db.local.properties）");
        login.setVisible(false);
        login.dispose();
        MainFrame main = show(() -> new MainFrame(teacher), 120, 120);
        shot(main, out.resolve("02-教师端主界面.png"));

        // 3) 录入成绩对话框
        JDialog dlg = openAddScoreDialog(main);
        if (dlg != null) {
            Thread.sleep(900);
            shot(dlg, out.resolve("03-录入成绩对话框.png"));
            dlg.dispose();
        }

        System.out.println("SHOTS_TEACHER_DONE");

        // 4) 学生端主界面（只读权限：仅本人成绩 + GPA 统计）
        User student = svc.login("student", "123456");
        if (student != null) {
            MainFrame studentFrame = show(() -> new MainFrame(student), 360, 300);
            shot(studentFrame, out.resolve("04-学生端主界面.png"));
            studentFrame.setVisible(false);
            studentFrame.dispose();
        }

        System.out.println("SHOTS_DONE " + out.toAbsolutePath());
        main.dispose();
        System.exit(0);
    }

    /** 通过 MainFrame 的公开行为生成录入对话框截图：后台线程点击按钮（模态对话框不阻塞主线程）。 */
    private static JDialog openAddScoreDialog(MainFrame main) throws Exception {
        JButton target = null;
        for (java.awt.Component c : main.getContentPane().getComponents()) {
            if (c instanceof JPanel p) {
                for (java.awt.Component b : p.getComponents()) {
                    if (b instanceof JButton btn && "录入成绩".equals(btn.getText())) {
                        target = btn;
                    }
                }
            }
        }
        if (target == null) return null;
        Thread clicker = new Thread(target::doClick);
        clicker.start();
        // 等待模态对话框出现
        for (int i = 0; i < 30; i++) {
            for (Window w : Window.getWindows()) {
                if (w instanceof JDialog d && d.isVisible()) return d;
            }
            Thread.sleep(100);
        }
        return null;
    }

    private static <T extends JFrame> T show(Supplier<T> factory, int x, int y) throws Exception {
        final T[] frame = (T[]) new JFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            T f = factory.get();
            f.setLocation(x, y);
            f.setVisible(true);
            frame[0] = f;
        });
        Thread.sleep(1500);
        return frame[0];
    }

    private static void shot(JFrame f, Path png) throws Exception {
        Robot robot = new Robot();
        Rectangle bounds = f.getBounds();
        // 补上 Windows 标题栏高度
        Rectangle r = new Rectangle(bounds.x, bounds.y - 32, bounds.width, bounds.height + 32);
        BufferedImage img = robot.createScreenCapture(r);
        javax.imageio.ImageIO.write(img, "png", png.toFile());
        System.out.println("SHOT " + png.getFileName());
    }

    private static void shot(JDialog d, Path png) throws Exception {
        Robot robot = new Robot();
        Rectangle b = d.getBounds();
        BufferedImage img = robot.createScreenCapture(new Rectangle(b.x, b.y - 32, b.width, b.height + 32));
        javax.imageio.ImageIO.write(img, "png", png.toFile());
        System.out.println("SHOT " + png.getFileName());
    }
}
