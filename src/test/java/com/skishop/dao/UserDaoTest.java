package com.skishop.dao;

import com.skishop.dao.user.UserDao;
import com.skishop.domain.user.User;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest extends DaoTestBase {

    @Autowired
    private UserDao userDao;

    @Test
    void testFindByEmail() {
        User user = userDao.findByEmail("user@example.com");
        assertNotNull(user);
        assertEquals("u-1", user.getId());
    }

    @Test
    void testInsertAndUpdateStatus() {
        User user = new User();
        user.setId("u-2");
        user.setEmail("new@example.com");
        user.setUsername("new");
        user.setPasswordHash("hash2");
        user.setSalt("salt2");
        user.setStatus("ACTIVE");
        user.setRole("USER");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userDao.insert(user);

        User loaded = userDao.findById("u-2");
        assertNotNull(loaded);
        assertEquals("new@example.com", loaded.getEmail());

        userDao.updateStatus("u-2", "LOCKED");
        User updated = userDao.findById("u-2");
        assertEquals("LOCKED", updated.getStatus());
    }
}
