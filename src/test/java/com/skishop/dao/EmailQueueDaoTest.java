package com.skishop.dao;

import com.skishop.dao.mail.EmailQueueDao;
import com.skishop.domain.mail.EmailQueue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class EmailQueueDaoTest extends DaoTestBase {

    @Autowired
    private EmailQueueDao emailQueueDao;

    @Test
    void testEnqueueAndFindByStatus() {
        List<EmailQueue> pending = emailQueueDao.findByStatus("PENDING");
        assertNotNull(pending);
    }
}
