package com.swd.score.model;

/** 成绩明细视图模型：JOIN 查询后的一行展示数据（学号/姓名/班级/课程/分数/考试日期）。 */
public class ScoreView {
    private String stuNo;
    private String stuName;
    private String className;
    private String courseName;
    private double score;
    private String examDate;

    public ScoreView() {}

    public ScoreView(String stuNo, String stuName, String className,
                     String courseName, double score, String examDate) {
        this.stuNo = stuNo;
        this.stuName = stuName;
        this.className = className;
        this.courseName = courseName;
        this.score = score;
        this.examDate = examDate;
    }

    public String getStuNo() { return stuNo; }
    public String getStuName() { return stuName; }
    public String getClassName() { return className; }
    public String getCourseName() { return courseName; }
    public double getScore() { return score; }
    public String getExamDate() { return examDate; }
}
