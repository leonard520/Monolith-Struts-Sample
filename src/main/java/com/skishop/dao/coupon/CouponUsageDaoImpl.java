package com.skishop.dao.coupon;

import com.skishop.domain.coupon.CouponUsage;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CouponUsageDaoImpl implements CouponUsageDao {
    private final JdbcTemplate jdbcTemplate;

    public CouponUsageDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(CouponUsage usage) {
        jdbcTemplate.update(
            "INSERT INTO coupon_usage(id, coupon_id, user_id, order_id, discount_applied, used_at) VALUES(?,?,?,?,?,?)",
            usage.getId(), usage.getCouponId(), usage.getUserId(), usage.getOrderId(),
            usage.getDiscountApplied(), toTimestamp(usage.getUsedAt()));
    }

    public CouponUsage findByOrderId(String orderId) {
        var results = jdbcTemplate.query(
            "SELECT id, coupon_id, user_id, order_id, discount_applied, used_at FROM coupon_usage WHERE order_id = ?",
            (rs, rowNum) -> {
                CouponUsage u = new CouponUsage();
                u.setId(rs.getString("id"));
                u.setCouponId(rs.getString("coupon_id"));
                u.setUserId(rs.getString("user_id"));
                u.setOrderId(rs.getString("order_id"));
                u.setDiscountApplied(rs.getBigDecimal("discount_applied"));
                u.setUsedAt(rs.getTimestamp("used_at"));
                return u;
            }, orderId);
        return results.isEmpty() ? null : results.get(0);
    }

    public void deleteByOrderId(String orderId) {
        jdbcTemplate.update("DELETE FROM coupon_usage WHERE order_id = ?", orderId);
    }

    private Timestamp toTimestamp(java.util.Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }
}
