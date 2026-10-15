package com.swd.score.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** 成绩数据访问。提供录入（存在则更新）、联表查询与课程平均分。 */
public class ScoreDao {

    /** 联表展示用的轻量视图对象。 */
    public static class ScoreView {
        public String studentNo;
        public String name;
        public String courseName;
        public double value;
    }

    /** 录入或更新（同一学生同一课程只保留一条记录）。 */
    public void upsert(int studentId, int courseId, double value) throws Exception {
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE score SET value=? WHERE student_id=? AND course_id=?")) {
                ps.setDouble(1, value);
                ps.setInt(2, studentId);
                ps.setInt(3, courseId);
                if (ps.executeUpdate() == 0) {
                    try (PreparedStatement pi = c.prepareStatement(
                            "INSERT INTO score(student_id,course_id,value) VALUES(?,?,?)")) {
                        pi.setInt(1, studentId);
                        pi.setInt(2, courseId);
                        pi.setDouble(3, value);
                        pi.executeUpdate();
                    }
                }
            }
        }
    }

    /** 学生-课程-成绩联表结果，用于导出与展示。 */
    public List<ScoreView> allWithNames() throws Exception {
        List<ScoreView> list = new ArrayList<>();
        String sql = "SELECT s.student_no, s.name, c.name AS course, sc.value "
                   + "FROM score sc "
                   + "JOIN student s ON s.id=sc.student_id "
                   + "JOIN course c ON c.id=sc.course_id "
                   + "ORDER BY s.id, c.id";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ScoreView v = new ScoreView();
                v.studentNo = rs.getString("student_no");
                v.name = rs.getString("name");
                v.courseName = rs.getString("course");
                v.value = rs.getDouble("value");
                list.add(v);
            }
        }
        return list;
    }

    /** 某课程的全体平均分。 */
    public double avgByCourse(int courseId) throws Exception {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT AVG(value) FROM score WHERE course_id=?")) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    /** 某学生的 成绩-学分 明细，用于 GPA 计算。 */
    public List<double[]> valuesWithCredit(int studentId) throws Exception {
        List<double[]> rows = new ArrayList<>();
        String sql = "SELECT sc.value, c.credit FROM score sc "
                   + "JOIN course c ON c.id=sc.course_id WHERE sc.student_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new double[]{rs.getDouble("value"), rs.getDouble("credit")});
                }
            }
        }
        return rows;
    }
}
