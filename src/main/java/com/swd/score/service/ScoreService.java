package com.swd.score.service;

import com.swd.score.dao.ScoreDao;
import com.swd.score.dao.UserDao;
import com.swd.score.model.ScoreView;
import com.swd.score.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务层：登录鉴权、成绩查询/录入、统计（平均分 / 排名 / GPA）。
 * 统计函数均为纯函数（静态方法），便于单元测试。
 */
public class ScoreService {

    private final UserDao userDao = new UserDao();
    private final ScoreDao scoreDao = new ScoreDao();

    // ---------- 登录 ----------

    /** 校验用户名/口令；成功返回 User，失败返回 null。口令以 SHA-256 摘要比对。 */
    public User login(String username, String password) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }
        try {
            return userDao.findByUsername(username.trim())
                    .filter(u -> u.getPasswordHash().equalsIgnoreCase(sha256(password)))
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("[login] " + e.getMessage());
            return null;
        }
    }

    // ---------- 查询 ----------

    public List<ScoreView> allRows() {
        try { return scoreDao.findAll(); }
        catch (Exception e) { throw new IllegalStateException("查询失败: " + e.getMessage(), e); }
    }

    public List<ScoreView> rowsForStudent(String stuNo) {
        try { return scoreDao.findByStudentNo(stuNo); }
        catch (Exception e) { throw new IllegalStateException("查询失败: " + e.getMessage(), e); }
    }

    public Map<String, Double> classAverages() {
        try { return scoreDao.classAverages(); }
        catch (Exception e) { throw new IllegalStateException("统计失败: " + e.getMessage(), e); }
    }

    public Map<String, Double> courseAverages() {
        try { return scoreDao.courseAverages(); }
        catch (Exception e) { throw new IllegalStateException("统计失败: " + e.getMessage(), e); }
    }

    /** 教师录入成绩；学生不存在时自动建档。 */
    public boolean addScore(String stuNo, String course, double value, String examDate) {
        if (stuNo == null || stuNo.isBlank() || course == null || course.isBlank()) {
            return false;
        }
        try { return scoreDao.upsert(stuNo.trim(), course.trim(), value, examDate); }
        catch (Exception e) { throw new IllegalStateException("录入失败: " + e.getMessage(), e); }
    }

    // ---------- 统计（纯函数，可单测） ----------

    /** 一组成绩的平均分；空集合返回 0.0。 */
    public static double average(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /** 按学生汇总：学号 -> 平均分（保持首见顺序）。 */
    public static Map<String, Double> averageByStudent(List<ScoreView> rows) {
        Map<String, List<Double>> grouped = new LinkedHashMap<>();
        for (ScoreView r : rows) {
            grouped.computeIfAbsent(r.getStuNo(), k -> new java.util.ArrayList<>()).add(r.getScore());
        }
        Map<String, Double> out = new LinkedHashMap<>();
        grouped.forEach((k, v) -> out.put(k, round2(average(v))));
        return out;
    }

    /** 某学号在成绩明细中的平均分排名（1 = 最高）。 */
    public static int rankOf(List<ScoreView> rows, String stuNo) {
        Map<String, Double> avg = averageByStudent(rows);
        double mine = avg.getOrDefault(stuNo, 0.0);
        int rank = 1;
        for (Map.Entry<String, Double> e : avg.entrySet()) {
            if (!e.getKey().equals(stuNo) && e.getValue() > mine) rank++;
        }
        return rank;
    }

    /** 常见 4.0 分段 GPA（百分制 -> 4.0 制，纯函数）。 */
    public static double gpa4(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double sum = 0;
        for (double v : values) sum += toGpaPoint(v);
        return round2(sum / values.size());
    }

    static double toGpaPoint(double v) {
        if (v >= 90) return 4.0;
        if (v >= 85) return 3.7;
        if (v >= 82) return 3.3;
        if (v >= 78) return 3.0;
        if (v >= 75) return 2.7;
        if (v >= 72) return 2.3;
        if (v >= 68) return 2.0;
        if (v >= 64) return 1.5;
        if (v >= 60) return 1.0;
        return 0.0;
    }

    static double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
