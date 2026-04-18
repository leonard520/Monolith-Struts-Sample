package com.skishop.dao.point;

import com.skishop.domain.point.PointTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class PointTransactionDaoImpl implements PointTransactionDao {

    private final JdbcTemplate jdbc;

    public PointTransactionDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(PointTransaction transaction) {
        jdbc.update("INSERT INTO point_transactions(id, user_id, reference_id, type, amount, description, created_at) VALUES(?,?,?,?,?,?,?)",
                transaction.getId(), transaction.getUserId(), transaction.getReferenceId(),
                transaction.getType(), transaction.getAmount(), transaction.getDescription(),
                toTimestamp(transaction.getCreatedAt()));
    }

    @Override
    public List<PointTransaction> listByUserId(String userId) {
        return jdbc.query(
                "SELECT id, user_id, reference_id, type, amount, description, created_at FROM point_transactions WHERE user_id = ? ORDER BY created_at DESC",
                (rs, rowNum) -> mapTransaction(rs), userId);
    }

    private PointTransaction mapTransaction(ResultSet rs) throws SQLException {
        PointTransaction txn = new PointTransaction();
        txn.setId(rs.getString("id"));
        txn.setUserId(rs.getString("user_id"));
        txn.setReferenceId(rs.getString("reference_id"));
        txn.setType(rs.getString("type"));
        txn.setAmount(rs.getInt("amount"));
        txn.setDescription(rs.getString("description"));
        txn.setCreatedAt(rs.getTimestamp("created_at"));
        return txn;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
