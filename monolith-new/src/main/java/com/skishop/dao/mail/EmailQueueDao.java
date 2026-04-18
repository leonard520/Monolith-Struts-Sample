package com.skishop.dao.mail;

import com.skishop.domain.mail.EmailQueue;
import java.util.List;

public interface EmailQueueDao {
    void enqueue(EmailQueue email);
    void updateStatus(String id, String status, int retryCount, String lastError, java.util.Date scheduledAt, java.util.Date sentAt);
    List<EmailQueue> findByStatus(String status);
}
