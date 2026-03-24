package com.skishop.dao;

import com.skishop.dao.user.PasswordResetTokenDao;
import com.skishop.domain.user.PasswordResetToken;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenDaoTest extends DaoTestBase {

    @Autowired
    private PasswordResetTokenDao tokenDao;

    @Test
    void testInsertAndFindByToken() {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setId("prt-test-99");
        prt.setUserId("u-1");
        prt.setToken("test-token");
        prt.setExpiresAt(new Date(System.currentTimeMillis() + 3600000));
        tokenDao.insert(prt);

        PasswordResetToken loaded = tokenDao.findByToken("test-token");
        assertNotNull(loaded);
        assertEquals("u-1", loaded.getUserId());
    }
}
