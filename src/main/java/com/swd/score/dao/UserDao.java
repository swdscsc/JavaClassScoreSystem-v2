package com.swd.score.dao;

import com.swd.score.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** 用户表访问：按用户名查询登录用户。 */
public class UserDao {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, role, display_name FROM users WHERE username = ?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("display_name")));
                }
                return Optional.empty();
            }
        }
    }
}
