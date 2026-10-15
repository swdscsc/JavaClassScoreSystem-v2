-- 学生成绩管理系统建表脚本（SQLite）
-- 由 com.swd.score.dao.Database 在首次连接时自动执行；此处仅作文档与手动初始化参考。

CREATE TABLE IF NOT EXISTS student (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    student_no  TEXT    UNIQUE NOT NULL,
    name        TEXT    NOT NULL,
    gender      TEXT,
    class_name  TEXT
);

CREATE TABLE IF NOT EXISTS course (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    code   TEXT    UNIQUE NOT NULL,
    name   TEXT    NOT NULL,
    credit REAL    NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS score (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    course_id  INTEGER NOT NULL,
    value      REAL    NOT NULL,
    UNIQUE(student_id, course_id),
    FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY(course_id)  REFERENCES course(id)  ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_score_student ON score(student_id);
CREATE INDEX IF NOT EXISTS idx_score_course  ON score(course_id);
