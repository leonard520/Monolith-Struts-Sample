package com.skishop.dao.coupon;

import com.skishop.domain.coupon.Coupon;
import java.util.List;

public interface CouponDao {
    Coupon findByCode(String code);
    List<Coupon> listActive();
    List<Coupon> listAll();
    void saveOrUpdate(Coupon coupon);
    void incrementUsedCount(String couponId);
    void decrementUsedCount(String couponId);
}
