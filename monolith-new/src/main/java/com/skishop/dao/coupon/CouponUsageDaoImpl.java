package com.skishop.dao.coupon;

import com.skishop.domain.coupon.CouponUsage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class CouponUsageDaoImpl implements CouponUsageDao {

    private final JdbcTemplate jdbc;

    public CouponUsageDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(CouponUsage usage) {
        jdbc.update("INSERT INTO coupon_usage(id, coupon_id, user_id, order_id, discount_applied, used_at) VALUES(?,?,?,?,?,?)",
                usage.getId(), usage.getCouponId(), usage.getUserId(), usage.getOrderId(),
                usage.getDiscountApplied(), toTimestamp(usage.getUsedAt()));
    }

    @Override
    public CouponUsage findByOrderId(String orderId) {
        List<CouponUsage> usages = jdbc.query(
                "SELECT id, coupon_id, user_id, order_id, discount_applied, used_at FROM coupon_usage WHERE order_id = ?",
                (rs, rowNum) -> mapUsage(rs), orderId);
        return usages.isEmpty() ? null : usages.get(0);
    }

    @Override
    public void deleteByOrderId(String orderId) {
        jdbc.update("DELETE FROM coupon_usage WHERE order_id = ?", orderId);
    }

    private CouponUsage mapUsage(ResultSet rs) throws SQLException {
        CouponUsage usage = new CouponUsage();
        usage.setId(rs.getString("id"));
        usage.setCouponId(rs.getString("coupon_id"));
        usage.setUserId(rs.getString("user_id"));
        usage.setOrderId(rs.getString("order_id"));
        usage.setDiscountApplied(rs.getBigDecimal("discount_applied"));
        usage.setUsedAt(rs.getTimestamp("used_at"));
        return usage;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
