package com.swd.score.dao;

import com.swd.score.model.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** 课程数据访问。 */
public class CourseDao {

    public void insert(Course c) throws Exception {
        String sql = "INSERT INTO course(code,name,credit) VALUES(?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getName());
            ps.setDouble(3, c.getCredit());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
        }
    }

    public List<Course> findAll() throws Exception {
        List<Course> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id,code,name,credit FROM course ORDER BY id")) {
            while (rs.next()) {
                Course c = new Course();
                c.setId(rs.getInt("id"));
                c.setCode(rs.getString("code"));
                c.setName(rs.getString("name"));
                c.setCredit(rs.getDouble("credit"));
                list.add(c);
            }
        }
        return list;
    }

    public Course findByCode(String code) throws Exception {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT id,code,name,credit FROM course WHERE code=?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Course c = new Course();
                    c.setId(rs.getInt("id"));
                    c.setCode(rs.getString("code"));
                    c.setName(rs.getString("name"));
                    c.setCredit(rs.getDouble("credit"));
                    return c;
                }
            }
        }
        return null;
    }
}
