package com.swd.score.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接管理（MySQL）。
 *
 * 配置优先级：
 * 1. 工作目录下的 db.local.properties（含真实口令，已被 .gitignore 排除，不入库）；
 * 2. classpath 内 resources/db.properties（提交到仓库的占位模板，真实口令用环境变量 DB_USER / DB_PASS 注入）。
 *
 * 建库脚本：src/main/resources/schema_mysql.sql
 *   mysql -uroot -p < schema_mysql.sql
 */
public final class Database {

    private static final Properties CFG = load();

    private Database() {}

    private static Properties load() {
        Properties p = new Properties();
        // 1) 本地真实配置（不入库）
        Path local = Path.of("db.local.properties");
        if (Files.exists(local)) {
            try (InputStream in = Files.newInputStream(local)) {
                p.load(in);
                return p;
            } catch (IOException ignored) { }
        }
        // 2) 仓库模板 + 环境变量
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException ignored) { }
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");
        if (user != null && !user.isBlank()) {
            p.setProperty("db.user", user);
            p.setProperty("db.password", pass == null ? "" : pass);
        }
        return p;
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(
                CFG.getProperty("db.url", "jdbc:mysql://localhost:3306/score_system"
                        + "?useSSL=false&allowPublicKeyRetrieval=true"
                        + "&serverTimezone=Asia/Shanghai&characterEncoding=utf8"),
                CFG.getProperty("db.user", "root"),
                CFG.getProperty("db.password", ""));
    }

    /** 演示环境口令是否已配置。 */
    public static boolean isConfigured() {
        String u = CFG.getProperty("db.user", "");
        return !u.isBlank() && !u.equals("CHANGE_ME");
    }
}
