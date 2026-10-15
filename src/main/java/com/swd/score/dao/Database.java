package com.swd.score.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/** SQLite 连接管理 + 建表。单例连接，首次使用时自动初始化 schema。 */
public final class Database {
    private static final String URL = "jdbc:sqlite:score.db";
    private static Connection conn;

    private Database() {}

    public static Connection getConnection() throws Exception {
        if (conn == null || conn.isClosed()) {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);
            initSchema();
        }
        return conn;
    }

    private static void initSchema() throws Exception {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS student (
                  id          INTEGER PRIMARY KEY AUTOINCREMENT,
                  student_no  TEXT    UNIQUE NOT NULL,
                  name        TEXT    NOT NULL,
                  gender      TEXT,
                  class_name  TEXT
                )""");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS course (
                  id     INTEGER PRIMARY KEY AUTOINCREMENT,
                  code   TEXT    UNIQUE NOT NULL,
                  name   TEXT    NOT NULL,
                  credit REAL    NOT NULL DEFAULT 0
                )""");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS score (
                  id         INTEGER PRIMARY KEY AUTOINCREMENT,
                  student_id INTEGER NOT NULL,
                  course_id  INTEGER NOT NULL,
                  value      REAL    NOT NULL,
                  UNIQUE(student_id, course_id),
                  FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE,
                  FOREIGN KEY(course_id)  REFERENCES course(id)  ON DELETE CASCADE
                )""");
        }
    }
}
