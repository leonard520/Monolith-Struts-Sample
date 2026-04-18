package com.skishop.dao.user;

import com.skishop.domain.user.PasswordResetToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class PasswordResetTokenDaoImpl implements PasswordResetTokenDao {

    private final JdbcTemplate jdbc;

    public PasswordResetTokenDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(PasswordResetToken token) {
        jdbc.update("INSERT INTO password_reset_tokens(id, user_id, token, expires_at, used_at) VALUES(?,?,?,?,?)",
                token.getId(), token.getUserId(), token.getToken(),
                toTimestamp(token.getExpiresAt()), toTimestamp(token.getUsedAt()));
    }

    @Override
    public PasswordResetToken findByToken(String tokenValue) {
        List<PasswordResetToken> tokens = jdbc.query(
                "SELECT id, user_id, token, expires_at, used_at FROM password_reset_tokens WHERE token = ?",
                (rs, rowNum) -> mapToken(rs), tokenValue);
        return tokens.isEmpty() ? null : tokens.get(0);
    }

    @Override
    public void markUsed(String tokenId) {
        jdbc.update("UPDATE password_reset_tokens SET used_at = ? WHERE id = ?",
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
        return date == null ? null : new Timestamp(date.getTime());
    }
}
