package com.skishop.dao.point;

import com.skishop.domain.point.PointAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PointAccountDaoImpl implements PointAccountDao {
    private final JdbcTemplate jdbcTemplate;

    public PointAccountDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PointAccount findByUserId(String userId) {
        var results = jdbcTemplate.query(
            "SELECT id, user_id, balance, lifetime_earned, lifetime_redeemed FROM point_accounts WHERE user_id = ?",
            (rs, rowNum) -> {
                PointAccount a = new PointAccount();
                a.setId(rs.getString("id"));
                a.setUserId(rs.getString("user_id"));
                a.setBalance(rs.getInt("balance"));
                a.setLifetimeEarned(rs.getInt("lifetime_earned"));
                a.setLifetimeRedeemed(rs.getInt("lifetime_redeemed"));
                return a;
            }, userId);
        return results.isEmpty() ? null : results.get(0);
    }

    public void insert(PointAccount account) {
        jdbcTemplate.update(
            "INSERT INTO point_accounts(id, user_id, balance, lifetime_earned, lifetime_redeemed) VALUES(?,?,?,?,?)",
            account.getId(), account.getUserId(), account.getBalance(),
            account.getLifetimeEarned(), account.getLifetimeRedeemed());
    }

    public void increment(String userId, int amount) {
        if (amount >= 0) {
            jdbcTemplate.update(
                "UPDATE point_accounts SET balance = balance + ?, lifetime_earned = lifetime_earned + ? WHERE user_id = ?",
                amount, amount, userId);
        } else {
            jdbcTemplate.update(
                "UPDATE point_accounts SET balance = balance + ?, lifetime_redeemed = lifetime_redeemed + ? WHERE user_id = ?",
                amount, Math.abs(amount), userId);
        }
    }
}
