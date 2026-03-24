package com.skishop.dao;

import com.skishop.dao.point.PointAccountDao;
import com.skishop.domain.point.PointAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class PointAccountDaoTest extends DaoTestBase {

    @Autowired
    private PointAccountDao pointAccountDao;

    @Test
    void testFindByUserId() {
        PointAccount account = pointAccountDao.findByUserId("u-1");
        assertNotNull(account);
    }
}
