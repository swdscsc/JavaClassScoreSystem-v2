package com.swd.score.dao;

import com.swd.score.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 课程表访问（MySQL）。 */
public class CourseDao {

    public List<Course> findAll() throws SQLException {
        String sql = "SELECT id, name FROM courses ORDER BY id";
        List<Course> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setName(rs.getString("name"));
                course.setCredit(0); // 学分列预留，当前演示库未启用
                out.add(course);
            }
        }
        return out;
    }
}
