package com.swd.score;

import com.swd.score.excel.ExcelExporter;
import com.swd.score.gui.DemoScreenshot;
import com.swd.score.gui.LoginFrame;
import com.swd.score.model.ScoreView;
import com.swd.score.service.ScoreService;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 入口：
 *   无参数 / gui                      —— 启动图形界面（登录 -> 角色主界面）
 *   --export <文件.xlsx>              —— 命令行导出成绩总表 Excel
 *   --stats                           —— 命令行打印班级/课程统计
 *   --demo-shot <输出目录>            —— 自动演示并截图（开发用）
 *   --help                            —— 帮助
 */
public final class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "gui".equals(args[0])) {
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            return;
        }
        switch (args[0]) {
            case "--export" -> {
                requireDb();
                String file = args.length > 1 ? args[1] : "成绩总表.xlsx";
                ScoreService svc = new ScoreService();
                List<ScoreView> rows = svc.allRows();
                new ExcelExporter().export(rows, svc.classAverages(), Path.of(file));
                System.out.println("已导出 " + rows.size() + " 条成绩 -> " + Path.of(file).toAbsolutePath());
            }
            case "--stats" -> {
                requireDb();
                ScoreService svc = new ScoreService();
                List<ScoreView> rows = svc.allRows();
                System.out.println("成绩条数: " + rows.size());
                System.out.println("-- 班级平均分 --");
                svc.classAverages().forEach((k, v) -> System.out.printf("  %s: %.2f%n", k, v));
                System.out.println("-- 课程平均分 --");
                svc.courseAverages().forEach((k, v) -> System.out.printf("  %s: %.2f%n", k, v));
                Map<String, Double> byStu = ScoreService.averageByStudent(rows);
                System.out.println("-- 学生平均分排名 --");
                byStu.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .forEach(e -> System.out.printf("  %-1s: %.2f (第 %d 名)%n",
                                e.getKey(), e.getValue(), ScoreService.rankOf(rows, e.getKey())));
            }
            case "--demo-shot" -> DemoScreenshot.run(
                    args.length > 1 ? args[1] : "docs/screenshots");
            case "--help" -> System.out.println("""
                    用法:
                      java com.swd.score.Main            # 启动图形界面
                      java com.swd.score.Main --export 成绩总表.xlsx
                      java com.swd.score.Main --stats
                      java com.swd.score.Main --demo-shot docs/screenshots
                    """);
            default -> System.err.println("未知参数: " + args[0] + "（--help 查看用法）");
        }
    }

    private static void requireDb() {
        if (!com.swd.score.dao.Database.isConfigured()) {
            System.err.println("数据库未配置：请在工作目录放置 db.local.properties（含 db.url/db.user/db.password），"
                    + "或设置环境变量 DB_USER / DB_PASS。建库脚本见 src/main/resources/schema_mysql.sql");
            System.exit(2);
        }
    }
}
