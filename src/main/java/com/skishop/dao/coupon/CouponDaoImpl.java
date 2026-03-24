package com.skishop.dao.coupon;

import com.skishop.domain.coupon.Coupon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CouponDaoImpl implements CouponDao {
    private final JdbcTemplate jdbcTemplate;

    public CouponDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Coupon findByCode(String code) {
        var results = jdbcTemplate.query(
            "SELECT id, campaign_id, code, coupon_type, discount_value, discount_type, minimum_amount, maximum_discount, usage_limit, used_count, is_active, expires_at FROM coupons WHERE code = ?",
            (rs, rowNum) -> mapCoupon(rs), code);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Coupon> listActive() {
        return jdbcTemplate.query(
            "SELECT id, campaign_id, code, coupon_type, discount_value, discount_type, minimum_amount, maximum_discount, usage_limit, used_count, is_active, expires_at FROM coupons WHERE is_active = ? AND (expires_at IS NULL OR expires_at > ?) ORDER BY code",
            (rs, rowNum) -> mapCoupon(rs), true, new Timestamp(System.currentTimeMillis()));
    }

    public List<Coupon> listAll() {
        return jdbcTemplate.query(
            "SELECT id, campaign_id, code, coupon_type, discount_value, discount_type, minimum_amount, maximum_discount, usage_limit, used_count, is_active, expires_at FROM coupons ORDER BY code",
            (rs, rowNum) -> mapCoupon(rs));
    }

    public void saveOrUpdate(Coupon coupon) {
        Coupon existing = findByCode(coupon.getCode());
        if (existing == null) {
            jdbcTemplate.update(
                "INSERT INTO coupons(id, campaign_id, code, coupon_type, discount_value, discount_type, minimum_amount, maximum_discount, usage_limit, used_count, is_active, expires_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                coupon.getId(), coupon.getCampaignId(), coupon.getCode(), coupon.getCouponType(),
                coupon.getDiscountValue(), coupon.getDiscountType(), coupon.getMinimumAmount(),
                coupon.getMaximumDiscount(), coupon.getUsageLimit(), coupon.getUsedCount(),
                coupon.isActive(), coupon.getExpiresAt() != null ? new Timestamp(coupon.getExpiresAt().getTime()) : null);
        } else {
            coupon.setId(existing.getId());
            coupon.setUsedCount(existing.getUsedCount());
            jdbcTemplate.update(
                "UPDATE coupons SET campaign_id = ?, coupon_type = ?, discount_value = ?, discount_type = ?, minimum_amount = ?, maximum_discount = ?, usage_limit = ?, used_count = ?, is_active = ?, expires_at = ? WHERE code = ?",
                coupon.getCampaignId(), coupon.getCouponType(), coupon.getDiscountValue(), coupon.getDiscountType(),
                coupon.getMinimumAmount(), coupon.getMaximumDiscount(), coupon.getUsageLimit(), coupon.getUsedCount(),
                coupon.isActive(), coupon.getExpiresAt() != null ? new Timestamp(coupon.getExpiresAt().getTime()) : null,
                coupon.getCode());
        }
    }

    public void incrementUsedCount(String couponId) {
        jdbcTemplate.update("UPDATE coupons SET used_count = used_count + ? WHERE id = ?", 1, couponId);
    }

    public void decrementUsedCount(String couponId) {
        jdbcTemplate.update("UPDATE coupons SET used_count = used_count + ? WHERE id = ?", -1, couponId);
    }

    private Coupon mapCoupon(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setId(rs.getString("id"));
        coupon.setCampaignId(rs.getString("campaign_id"));
        coupon.setCode(rs.getString("code"));
        coupon.setCouponType(rs.getString("coupon_type"));
        coupon.setDiscountValue(rs.getBigDecimal("discount_value"));
        coupon.setDiscountType(rs.getString("discount_type"));
        coupon.setMinimumAmount(rs.getBigDecimal("minimum_amount"));
        coupon.setMaximumDiscount(rs.getBigDecimal("maximum_discount"));
        coupon.setUsageLimit(rs.getInt("usage_limit"));
        coupon.setUsedCount(rs.getInt("used_count"));
        coupon.setActive(rs.getBoolean("is_active"));
        coupon.setExpiresAt(rs.getTimestamp("expires_at"));
        return coupon;
    }
}
