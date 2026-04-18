package com.skishop.dao.point;

import com.skishop.domain.point.PointAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PointAccountDaoImpl implements PointAccountDao {

    private final JdbcTemplate jdbc;

    public PointAccountDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public PointAccount findByUserId(String userId) {
        List<PointAccount> accounts = jdbc.query(
                "SELECT id, user_id, balance, lifetime_earned, lifetime_redeemed FROM point_accounts WHERE user_id = ?",
                (rs, rowNum) -> mapAccount(rs), userId);
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    @Override
    public void insert(PointAccount account) {
        jdbc.update("INSERT INTO point_accounts(id, user_id, balance, lifetime_earned, lifetime_redeemed) VALUES(?,?,?,?,?)",
                account.getId(), account.getUserId(), account.getBalance(),
                account.getLifetimeEarned(), account.getLifetimeRedeemed());
    }

    @Override
    public void increment(String userId, int amount) {
        String updateSql;
        int absoluteAmount;
        if (amount >= 0) {
            updateSql = "UPDATE point_accounts SET balance = balance + ?, lifetime_earned = lifetime_earned + ? WHERE user_id = ?";
            absoluteAmount = amount;
        } else {
            updateSql = "UPDATE point_accounts SET balance = balance + ?, lifetime_redeemed = lifetime_redeemed + ? WHERE user_id = ?";
            absoluteAmount = Math.abs(amount);
        }
        jdbc.update(updateSql, amount, absoluteAmount, userId);
    }

    private PointAccount mapAccount(ResultSet rs) throws SQLException {
        PointAccount account = new PointAccount();
        account.setId(rs.getString("id"));
        account.setUserId(rs.getString("user_id"));
        account.setBalance(rs.getInt("balance"));
        account.setLifetimeEarned(rs.getInt("lifetime_earned"));
        account.setLifetimeRedeemed(rs.getInt("lifetime_redeemed"));
        return account;
    }
}
