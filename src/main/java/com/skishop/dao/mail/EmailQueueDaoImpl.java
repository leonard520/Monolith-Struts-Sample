package com.skishop.dao.mail;

import com.skishop.domain.mail.EmailQueue;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmailQueueDaoImpl implements EmailQueueDao {
    private final JdbcTemplate jdbcTemplate;

    public EmailQueueDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void enqueue(EmailQueue mail) {
        jdbcTemplate.update(
            "INSERT INTO email_queue(id, to_addr, subject, body, status, retry_count, last_error, scheduled_at, sent_at) VALUES(?,?,?,?,?,?,?,?,?)",
            mail.getId(), mail.getToAddr(), mail.getSubject(), mail.getBody(), mail.getStatus(),
            mail.getRetryCount(), mail.getLastError(), toTimestamp(mail.getScheduledAt()), toTimestamp(mail.getSentAt()));
    }

    public void updateStatus(String id, String status, int retryCount, String lastError, Date scheduledAt, Date sentAt) {
        jdbcTemplate.update(
            "UPDATE email_queue SET status = ?, retry_count = ?, last_error = ?, scheduled_at = ?, sent_at = ? WHERE id = ?",
            status, retryCount, lastError, toTimestamp(scheduledAt), toTimestamp(sentAt), id);
    }

    public List<EmailQueue> findByStatus(String status) {
        return jdbcTemplate.query(
            "SELECT id, to_addr, subject, body, status, retry_count, last_error, scheduled_at, sent_at FROM email_queue WHERE status = ? ORDER BY scheduled_at",
            (rs, rowNum) -> {
                EmailQueue m = new EmailQueue();
                m.setId(rs.getString("id"));
                m.setToAddr(rs.getString("to_addr"));
                m.setSubject(rs.getString("subject"));
                m.setBody(rs.getString("body"));
                m.setStatus(rs.getString("status"));
                m.setRetryCount(rs.getInt("retry_count"));
                m.setLastError(rs.getString("last_error"));
                m.setScheduledAt(rs.getTimestamp("scheduled_at"));
                m.setSentAt(rs.getTimestamp("sent_at"));
                return m;
            }, status);
    }

    private Timestamp toTimestamp(Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }
}
