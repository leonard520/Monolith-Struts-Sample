package com.skishop.dao.user;

import com.skishop.domain.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao {
    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findByEmail(String email) {
        var results = jdbcTemplate.query(
            "SELECT id, email, username, password_hash, salt, status, role, created_at, updated_at FROM users WHERE email = ?",
            (rs, rowNum) -> mapUser(rs), email);
        return results.isEmpty() ? null : results.get(0);
    }

    public User findById(String id) {
        var results = jdbcTemplate.query(
            "SELECT id, email, username, password_hash, salt, status, role, created_at, updated_at FROM users WHERE id = ?",
            (rs, rowNum) -> mapUser(rs), id);
        return results.isEmpty() ? null : results.get(0);
    }

    public void insert(User user) {
        jdbcTemplate.update(
            "INSERT INTO users(id, email, username, password_hash, salt, status, role, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?)",
            user.getId(), user.getEmail(), user.getUsername(), user.getPasswordHash(),
            user.getSalt(), user.getStatus(), user.getRole(),
            toTimestamp(user.getCreatedAt()), toTimestamp(user.getUpdatedAt()));
    }

    public void updatePassword(String userId, String passwordHash, String salt) {
        jdbcTemplate.update("UPDATE users SET password_hash = ?, salt = ?, updated_at = ? WHERE id = ?",
            passwordHash, salt, new Timestamp(System.currentTimeMillis()), userId);
    }

    public void updateStatus(String userId, String status) {
        jdbcTemplate.update("UPDATE users SET status = ?, updated_at = ? WHERE id = ?",
            status, new Timestamp(System.currentTimeMillis()), userId);
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setEmail(rs.getString("email"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setStatus(rs.getString("status"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }
}
