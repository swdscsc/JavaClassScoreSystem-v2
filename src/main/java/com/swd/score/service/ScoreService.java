package com.swd.score.service;

import com.swd.score.dao.*;
import com.swd.score.model.Course;
import com.swd.score.model.Student;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 业务逻辑层：编排 DAO，提供学生/课程/成绩的增删查与统计、GPA、排名、导出。
 * UI（Main）与测试都只依赖本层，便于替换为图形界面或 Web 接口。
 */
public class ScoreService {
    private final StudentDao studentDao = new StudentDao();
    private final CourseDao courseDao = new CourseDao();
    private final ScoreDao scoreDao = new ScoreDao();

    // ---- 写操作 ----
    public void addStudent(Student s) throws Exception { studentDao.insert(s); }
    public void addCourse(Course c) throws Exception { courseDao.insert(c); }

    /** 按学号 + 课程代码录入成绩（解析不到则返回错误信息）。 */
    public String recordScore(String studentNo, String courseCode, double value) throws Exception {
        Student s = studentDao.findByNo(studentNo);
        if (s == null) return "未找到学号: " + studentNo;
        Course c = courseDao.findByCode(courseCode);
        if (c == null) return "未找到课程代码: " + courseCode;
        scoreDao.upsert(s.getId(), c.getId(), value);
        return "已录入 " + s.getName() + " 的《" + c.getName() + "》成绩: " + value;
    }

    // ---- 读 / 统计 ----
    public List<Student> listStudents() throws Exception { return studentDao.findAll(); }
    public List<Course> listCourses() throws Exception { return courseDao.findAll(); }

    /** 班级平均分（按学生 class_name 聚合成绩）。 */
    public double classAverage(String className) throws Exception {
        String sql = "SELECT AVG(sc.value) FROM score sc "
                   + "JOIN student s ON s.id=sc.student_id WHERE s.class_name=?";
        try (var c = Database.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, className);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    public double courseAverage(String courseCode) throws Exception {
        Course c = courseDao.findByCode(courseCode);
        if (c == null) return -1;
        return scoreDao.avgByCourse(c.getId());
    }

    /** 将百分制分数映射为 4.0 绩点（常用分段线性近似）。 */
    public static double scoreToPoint(double score) {
        if (score >= 90) return 4.0;
        if (score >= 85) return 3.7;
        if (score >= 82) return 3.3;
        if (score >= 78) return 3.0;
        if (score >= 75) return 2.7;
        if (score >= 72) return 2.3;
        if (score >= 68) return 2.0;
        if (score >= 64) return 1.5;
        if (score >= 60) return 1.0;
        return 0.0;
    }

    /** 学分加权平均绩点（4.0 制）。 */
    public double gpa(String studentNo) throws Exception {
        Student s = studentDao.findByNo(studentNo);
        if (s == null) return -1;
        List<double[]> rows = scoreDao.valuesWithCredit(s.getId());
        double ptSum = 0, crSum = 0;
        for (double[] r : rows) {
            double point = scoreToPoint(r[0]);
            double credit = r[1];
            ptSum += point * credit;
            crSum += credit;
        }
        return crSum > 0 ? ptSum / crSum : 0.0;
    }

    /** 排名条目：学生姓名 + 4.0 制 GPA。 */
    public static class RankEntry {
        public String name;
        public double gpa;
    }

    /** 对所有已录入成绩的学生按 GPA 降序排名。 */
    public List<RankEntry> ranking() throws Exception {
        List<RankEntry> result = new ArrayList<>();
        for (Student s : studentDao.findAll()) {
            List<double[]> rows = scoreDao.valuesWithCredit(s.getId());
            if (rows.isEmpty()) continue;
            double ptSum = 0, crSum = 0;
            for (double[] r : rows) {
                ptSum += scoreToPoint(r[0]) * r[1];
                crSum += r[1];
            }
            RankEntry e = new RankEntry();
            e.name = s.getName();
            e.gpa = crSum > 0 ? ptSum / crSum : 0.0;
            result.add(e);
        }
        result.sort(Comparator.comparingDouble((RankEntry e) -> e.gpa).reversed());
        return result;
    }

    /** 将全部成绩明细导出为 UTF-8 CSV。 */
    public void exportCsv(String path) throws Exception {
        List<ScoreDao.ScoreView> rows = scoreDao.allWithNames();
        try (PrintWriter w = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            w.println("student_no,name,course,value");
            for (ScoreDao.ScoreView v : rows) {
                w.println(v.studentNo + "," + v.name + "," + v.courseName + "," + v.value);
            }
        }
    }
}
