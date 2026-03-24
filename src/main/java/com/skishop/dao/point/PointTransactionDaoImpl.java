package com.skishop.dao.point;

import com.skishop.domain.point.PointTransaction;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PointTransactionDaoImpl implements PointTransactionDao {
    private final JdbcTemplate jdbcTemplate;

    public PointTransactionDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(PointTransaction t) {
        jdbcTemplate.update(
            "INSERT INTO point_transactions(id, user_id, type, amount, reference_id, description, expires_at, is_expired, created_at) VALUES(?,?,?,?,?,?,?,?,?)",
            t.getId(), t.getUserId(), t.getType(), t.getAmount(), t.getReferenceId(),
            t.getDescription(), toTimestamp(t.getExpiresAt()), t.isExpired(), toTimestamp(t.getCreatedAt()));
    }

    public List<PointTransaction> listByUserId(String userId) {
        return jdbcTemplate.query(
            "SELECT id, user_id, type, amount, reference_id, description, expires_at, is_expired, created_at FROM point_transactions WHERE user_id = ? ORDER BY created_at DESC",
            (rs, rowNum) -> {
                PointTransaction t = new PointTransaction();
                t.setId(rs.getString("id"));
                t.setUserId(rs.getString("user_id"));
                t.setType(rs.getString("type"));
                t.setAmount(rs.getInt("amount"));
                t.setReferenceId(rs.getString("reference_id"));
                t.setDescription(rs.getString("description"));
                t.setExpiresAt(rs.getTimestamp("expires_at"));
                t.setExpired(rs.getBoolean("is_expired"));
                t.setCreatedAt(rs.getTimestamp("created_at"));
                return t;
            }, userId);
    }

    private Timestamp toTimestamp(java.util.Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }
}
