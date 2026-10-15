-- =====================================================
-- 学生成绩管理系统 · MySQL 5.7 建库脚本
-- 数据库: score_system  字符集: utf8mb4
-- 运行: mysql -uroot -p < schema_mysql.sql
-- =====================================================
CREATE DATABASE IF NOT EXISTS score_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE score_system;

-- 用户表（登录 + 角色权限）
CREATE TABLE IF NOT EXISTS users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(32)  NOT NULL UNIQUE,
    password     CHAR(64)     NOT NULL COMMENT 'SHA-256 加密存储',
    role         ENUM('teacher','student') NOT NULL DEFAULT 'student',
    display_name VARCHAR(32)  NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 学生表
CREATE TABLE IF NOT EXISTS students (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    stu_no   VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    stu_name VARCHAR(32) NOT NULL,
    clazz    VARCHAR(32) NOT NULL COMMENT '班级'
) ENGINE=InnoDB;

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- 成绩表
CREATE TABLE IF NOT EXISTS scores (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    stu_id    INT  NOT NULL,
    course_id INT  NOT NULL,
    score     DECIMAL(5,1) NOT NULL,
    exam_date DATE NOT NULL,
    UNIQUE KEY uk_stu_course (stu_id, course_id),
    FOREIGN KEY (stu_id)    REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB;

-- =====================================================
-- 种子数据：密码均为 123456 的 SHA-256
-- =====================================================
INSERT INTO users (username, password, role, display_name) VALUES
('teacher', SHA2('123456', 256), 'teacher', '王全辉（教师）'),
('student', SHA2('123456', 256), 'student', '沈炜丹（学生）')
ON DUPLICATE KEY UPDATE password=VALUES(password);

INSERT INTO courses (name) VALUES
('Java 程序设计'), ('数据结构'), ('数据库原理'), ('操作系统')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO students (stu_no, stu_name, clazz) VALUES
('2023083101', '张一帆', '计科2301'), ('2023083102', '李雨桐', '计科2301'),
('2023083103', '陈子昂', '计科2301'), ('2023083201', '刘思远', '计科2302'),
('2023083202', '黄诗涵', '计科2302'), ('2023083203', '吴承宇', '计科2302'),
('2023083301', '赵芯语', '计科2303'), ('2023083302', '孙浩然', '计科2303')
ON DUPLICATE KEY UPDATE stu_name=VALUES(stu_name);

INSERT IGNORE INTO scores (stu_id, course_id, score, exam_date)
SELECT s.id, c.id, ROUND(65 + RAND(s.id*100+c.id)*33, 1), '2026-06-20'
FROM students s CROSS JOIN courses c;
