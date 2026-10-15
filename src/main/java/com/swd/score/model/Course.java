package com.swd.score.model;

/** 课程模型。credit 为该课程的学分，用于 GPA 加权计算。 */
public class Course {
    private int id;
    private String code;
    private String name;
    private double credit;

    public Course() {}

    public Course(String code, String name, double credit) {
        this.code = code;
        this.name = name;
        this.credit = credit;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getCredit() { return credit; }
    public void setCredit(double credit) { this.credit = credit; }
}
