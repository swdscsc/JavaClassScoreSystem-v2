package com.swd.score.dao;

import com.swd.score.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 学生表访问（MySQL）。 */
public class StudentDao {

    public List<Student> findAll() throws SQLException {
        String sql = "SELECT id, stu_no, stu_name, clazz FROM students ORDER BY clazz, stu_no";
        List<Student> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                s.setStudentNo(rs.getString("stu_no"));
                s.setName(rs.getString("stu_name"));
                s.setClassName(rs.getString("clazz"));
                out.add(s);
            }
        }
        return out;
    }
}
