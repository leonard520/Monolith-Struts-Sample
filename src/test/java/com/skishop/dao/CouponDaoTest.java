package com.skishop.dao;

import com.skishop.dao.coupon.CouponDao;
import com.skishop.domain.coupon.Coupon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class CouponDaoTest extends DaoTestBase {

    @Autowired
    private CouponDao couponDao;

    @Test
    void testFindByCode() {
        Coupon coupon = couponDao.findByCode("SAVE10");
        assertNotNull(coupon);
    }
}
