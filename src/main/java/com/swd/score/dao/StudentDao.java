package com.swd.score.dao;

import com.swd.score.model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** 学生数据访问。 */
public class StudentDao {

    public void insert(Student s) throws Exception {
        String sql = "INSERT INTO student(student_no,name,gender,class_name) VALUES(?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getStudentNo());
            ps.setString(2, s.getName());
            ps.setString(3, s.getGender());
            ps.setString(4, s.getClassName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setId(rs.getInt(1));
            }
        }
    }

    public List<Student> findAll() throws Exception {
        List<Student> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id,student_no,name,gender,class_name FROM student ORDER BY id")) {
            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                s.setStudentNo(rs.getString("student_no"));
                s.setName(rs.getString("name"));
                s.setGender(rs.getString("gender"));
                s.setClassName(rs.getString("class_name"));
                list.add(s);
            }
        }
        return list;
    }

    public Student findByNo(String no) throws Exception {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT id,student_no,name,gender,class_name FROM student WHERE student_no=?")) {
            ps.setString(1, no);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student();
                    s.setId(rs.getInt("id"));
                    s.setStudentNo(rs.getString("student_no"));
                    s.setName(rs.getString("name"));
                    s.setGender(rs.getString("gender"));
                    s.setClassName(rs.getString("class_name"));
                    return s;
                }
            }
        }
        return null;
    }

    public void delete(int id) throws Exception {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM student WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
