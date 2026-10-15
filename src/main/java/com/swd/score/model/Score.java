package com.swd.score.model;

/** 成绩记录模型：某学生在某课程的一次得分。 */
public class Score {
    private int id;
    private int studentId;
    private int courseId;
    private double value;

    public Score() {}

    public Score(int studentId, int courseId, double value) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.value = value;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
