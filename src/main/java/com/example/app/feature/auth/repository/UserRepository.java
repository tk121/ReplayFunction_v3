package com.example.app.feature.auth.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import com.example.app.feature.auth.entity.User;

public class UserRepository {

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findByUserName(String userName) throws Exception {
        String sql =
                "select " +
                "    user_id, " +
                "    user_name, " +
                "    password, " +
                "    can_control, " +
                "    enabled " +
                "from \"user\" " +
                "where user_name = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userName);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                User user = new User();
                user.setUserId(Long.valueOf(rs.getLong("user_id")));
                user.setUserName(rs.getString("user_name"));
                user.setPassword(rs.getString("password"));
                user.setCanControl(rs.getBoolean("can_control"));
                user.setEnabled(rs.getBoolean("enabled"));
                return user;
            }
        }
    }
    
    public User findByUserId(String userId) throws Exception {
        if (userId == null || userId.trim().length() == 0) {
            return null;
        }

        String sql =
                "select " +
                "    user_id, " +
                "    user_name, " +
                "    password, " +
                "    can_control, " +
                "    enabled " +
                "from \"user\" " +
                "where user_id = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, Long.parseLong(userId));

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                User user = new User();
                user.setUserId(Long.valueOf(rs.getLong("user_id")));
                user.setUserName(rs.getString("user_name"));
                user.setPassword(rs.getString("password"));
                user.setCanControl(rs.getBoolean("can_control"));
                user.setEnabled(rs.getBoolean("enabled"));
                return user;
            }
        }
    }
}
