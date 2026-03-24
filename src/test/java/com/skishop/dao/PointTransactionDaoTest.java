package com.skishop.dao;

import com.skishop.dao.point.PointTransactionDao;
import com.skishop.domain.point.PointTransaction;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class PointTransactionDaoTest extends DaoTestBase {

    @Autowired
    private PointTransactionDao pointTransactionDao;

    @Test
    void testInsert() {
        PointTransaction pt = new PointTransaction();
        pt.setId("pt-test-99");
        pt.setUserId("u-1");
        pt.setType("EARN");
        pt.setAmount(50);
        pt.setDescription("Test earn");
        pt.setCreatedAt(new Date());
        pt.setExpiresAt(new Date(System.currentTimeMillis() + 86400000));
        pointTransactionDao.insert(pt);
    }
}
