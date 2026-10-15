package com.swd.score.model;

/** 学生模型。 */
public class Student {
    private int id;
    private String studentNo;
    private String name;
    private String gender;
    private String className;

    public Student() {}

    public Student(String studentNo, String name, String gender, String className) {
        this.studentNo = studentNo;
        this.name = name;
        this.gender = gender;
        this.className = className;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
