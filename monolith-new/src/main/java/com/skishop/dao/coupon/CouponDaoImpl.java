package com.skishop.dao.coupon;

import com.skishop.domain.coupon.Coupon;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class CouponDaoImpl implements CouponDao {

    private final JdbcTemplate jdbc;

    public CouponDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Coupon findByCode(String code) {
        List<Coupon> coupons = jdbc.query(
                "SELECT id, campaign_id, code, coupon_type, discount_value, discount_type, minimum_amount, maximum_discount, usage_limit, used_count, is_active, expires_at FROM coupons WHERE code = ?",
                (rs, rowNum) -> mapCoupon(rs), code);
        return coupons.isEmpty() ? null : coupons.get(0);
    }

    @Override
    public List<Coupon> listActive() {
        return jdbc.query(
                "SELECT id, campaign_id, code, coupon_type, discount_value, discount_type, minimum_amount, maximum_discount, usage_limit, used_count, is_active, expires_at FROM coupons WHERE is_active = ? AND (expires_at IS NULL OR expires_at > ?) ORDER BY code",
                (rs, rowNum) -> mapCoupon(rs), true, new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public List<Coupon> listAll() {
        return jdbc.query(
                "SELECT id, campaign_id, code, coupon_type, discount_value, discount_type, minimum_amount, maximum_discount, usage_limit, used_count, is_active, expires_at FROM coupons ORDER BY code",
                (rs, rowNum) -> mapCoupon(rs));
    }

    @Override
    public void saveOrUpdate(Coupon coupon) {
        Coupon existing = findByCode(coupon.getCode());
        if (existing == null) {
            jdbc.update("INSERT INTO coupons(id, campaign_id, code, coupon_type, discount_value, discount_type, minimum_amount, maximum_discount, usage_limit, used_count, is_active, expires_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                    coupon.getId(), coupon.getCampaignId(), coupon.getCode(), coupon.getCouponType(),
                    coupon.getDiscountValue(), coupon.getDiscountType(), coupon.getMinimumAmount(),
                    coupon.getMaximumDiscount(), coupon.getUsageLimit(), coupon.getUsedCount(),
                    coupon.isActive(), coupon.getExpiresAt() != null ? new Timestamp(coupon.getExpiresAt().getTime()) : null);
        } else {
            coupon.setId(existing.getId());
            coupon.setUsedCount(existing.getUsedCount());
            jdbc.update("UPDATE coupons SET campaign_id = ?, coupon_type = ?, discount_value = ?, discount_type = ?, minimum_amount = ?, maximum_discount = ?, usage_limit = ?, used_count = ?, is_active = ?, expires_at = ? WHERE code = ?",
                    coupon.getCampaignId(), coupon.getCouponType(), coupon.getDiscountValue(), coupon.getDiscountType(),
                    coupon.getMinimumAmount(), coupon.getMaximumDiscount(), coupon.getUsageLimit(), coupon.getUsedCount(),
                    coupon.isActive(), coupon.getExpiresAt() != null ? new Timestamp(coupon.getExpiresAt().getTime()) : null,
                    coupon.getCode());
        }
    }

    @Override
    public void incrementUsedCount(String couponId) {
        jdbc.update("UPDATE coupons SET used_count = used_count + ? WHERE id = ?", 1, couponId);
    }

    @Override
    public void decrementUsedCount(String couponId) {
        jdbc.update("UPDATE coupons SET used_count = used_count + ? WHERE id = ?", -1, couponId);
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
