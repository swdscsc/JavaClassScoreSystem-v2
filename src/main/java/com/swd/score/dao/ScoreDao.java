package com.swd.score.dao;

import com.swd.score.model.ScoreView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 成绩数据访问：明细查询、班级/课程统计、成绩写入（MySQL JOIN 实现）。 */
public class ScoreDao {

    private static final String JOIN_SQL =
            "SELECT s.stu_no, s.stu_name, s.clazz, c.name AS course, sc.score, sc.exam_date " +
            "FROM scores sc " +
            "JOIN students s ON s.id = sc.stu_id " +
            "JOIN courses  c ON c.id = sc.course_id ";

    /** 全部成绩明细。 */
    public List<ScoreView> findAll() throws SQLException {
        return query(JOIN_SQL + "ORDER BY s.clazz, s.stu_no, c.id");
    }

    /** 某学号学生的成绩明细。 */
    public List<ScoreView> findByStudentNo(String stuNo) throws SQLException {
        return query(JOIN_SQL + "WHERE s.stu_no = ? ORDER BY c.id", stuNo);
    }

    /** 班级 -> 平均分。 */
    public Map<String, Double> classAverages() throws SQLException {
        return avgBy("s.clazz");
    }

    /** 课程 -> 平均分。 */
    public Map<String, Double> courseAverages() throws SQLException {
        return avgBy("c.name");
    }

    /** 教师录入/更新一条成绩（同一学生同一课程只保留最新）。 */
    public boolean upsert(String stuNo, String courseName, double value, String examDate)
            throws SQLException {
        String ensureStudent =
                "INSERT IGNORE INTO students (stu_no, stu_name, clazz) VALUES (?, ?, '待分配')";
        String ensureCourse =
                "INSERT IGNORE INTO courses (name) VALUES (?)";
        String upsertScore =
                "INSERT INTO scores (stu_id, course_id, score, exam_date) " +
                "SELECT s.id, c.id, ?, ? FROM students s, courses c " +
                "WHERE s.stu_no = ? AND c.name = ? " +
                "ON DUPLICATE KEY UPDATE score = VALUES(score), exam_date = VALUES(exam_date)";
        try (Connection c = Database.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement p1 = c.prepareStatement(ensureStudent);
                 PreparedStatement p2 = c.prepareStatement(ensureCourse);
                 PreparedStatement p3 = c.prepareStatement(upsertScore)) {
                p1.setString(1, stuNo);
                p1.setString(2, "新增学生");
                p1.executeUpdate();
                p2.setString(1, courseName);
                p2.executeUpdate();
                p3.setDouble(1, value);
                p3.setString(2, examDate);
                p3.setString(3, stuNo);
                p3.setString(4, courseName);
                int n = p3.executeUpdate();
                c.commit();
                return n > 0;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    // ---------- 内部 ----------

    private List<ScoreView> query(String sql, Object... args) throws SQLException {
        List<ScoreView> rows = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ScoreView(
                            rs.getString("stu_no"),
                            rs.getString("stu_name"),
                            rs.getString("clazz"),
                            rs.getString("course"),
                            rs.getDouble("score"),
                            String.valueOf(rs.getDate("exam_date"))));
                }
            }
        }
        return rows;
    }

    private Map<String, Double> avgBy(String groupCol) throws SQLException {
        Map<String, Double> out = new LinkedHashMap<>();
        String sql = "SELECT " + groupCol + " AS g, ROUND(AVG(sc.score), 2) AS avg_score " +
                     "FROM scores sc JOIN students s ON s.id = sc.stu_id " +
                     "JOIN courses c ON c.id = sc.course_id GROUP BY " + groupCol + " ORDER BY g";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.put(rs.getString("g"), rs.getDouble("avg_score"));
            }
        }
        return out;
    }
}
