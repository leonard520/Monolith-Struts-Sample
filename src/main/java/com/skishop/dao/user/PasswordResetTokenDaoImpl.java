package com.skishop.dao.user;

import com.skishop.domain.user.PasswordResetToken;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PasswordResetTokenDaoImpl implements PasswordResetTokenDao {
    private final JdbcTemplate jdbcTemplate;

    public PasswordResetTokenDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(PasswordResetToken token) {
        jdbcTemplate.update(
            "INSERT INTO password_reset_tokens(id, user_id, token, expires_at, used_at) VALUES(?,?,?,?,?)",
            token.getId(), token.getUserId(), token.getToken(),
            toTimestamp(token.getExpiresAt()), toTimestamp(token.getUsedAt()));
    }

    public PasswordResetToken findByToken(String tokenValue) {
        var results = jdbcTemplate.query(
            "SELECT id, user_id, token, expires_at, used_at FROM password_reset_tokens WHERE token = ?",
            (rs, rowNum) -> mapToken(rs), tokenValue);
        return results.isEmpty() ? null : results.get(0);
    }

    public void markUsed(String tokenId) {
        jdbcTemplate.update("UPDATE password_reset_tokens SET used_at = ? WHERE id = ?",
            new Timestamp(System.currentTimeMillis()), tokenId);
    }

    private PasswordResetToken mapToken(ResultSet rs) throws SQLException {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(rs.getString("id"));
        token.setUserId(rs.getString("user_id"));
        token.setToken(rs.getString("token"));
        token.setExpiresAt(rs.getTimestamp("expires_at"));
        token.setUsedAt(rs.getTimestamp("used_at"));
        return token;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }
}
