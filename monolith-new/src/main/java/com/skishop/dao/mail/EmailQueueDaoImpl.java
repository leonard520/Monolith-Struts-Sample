package com.skishop.dao.mail;

import com.skishop.domain.mail.EmailQueue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Repository
public class EmailQueueDaoImpl implements EmailQueueDao {

    private final JdbcTemplate jdbc;

    public EmailQueueDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void enqueue(EmailQueue email) {
        jdbc.update("INSERT INTO email_queue(id, to_addr, subject, body, status, retry_count, last_error, scheduled_at, sent_at) VALUES(?,?,?,?,?,?,?,?,?)",
                email.getId(), email.getToAddr(), email.getSubject(), email.getBody(),
                email.getStatus(), email.getRetryCount(), email.getLastError(),
                toTimestamp(email.getScheduledAt()), toTimestamp(email.getSentAt()));
    }

    @Override
    public void updateStatus(String id, String status, int retryCount, String lastError, Date scheduledAt, Date sentAt) {
        jdbc.update("UPDATE email_queue SET status = ?, retry_count = ?, last_error = ?, scheduled_at = ?, sent_at = ? WHERE id = ?",
                status, retryCount, lastError, toTimestamp(scheduledAt), toTimestamp(sentAt), id);
    }

    @Override
    public List<EmailQueue> findByStatus(String status) {
        return jdbc.query(
                "SELECT id, to_addr, subject, body, status, retry_count, last_error, scheduled_at, sent_at FROM email_queue WHERE status = ? ORDER BY scheduled_at",
                (rs, rowNum) -> mapEmail(rs), status);
    }

    private EmailQueue mapEmail(ResultSet rs) throws SQLException {
        EmailQueue email = new EmailQueue();
        email.setId(rs.getString("id"));
        email.setToAddr(rs.getString("to_addr"));
        email.setSubject(rs.getString("subject"));
        email.setBody(rs.getString("body"));
        email.setStatus(rs.getString("status"));
        email.setRetryCount(rs.getInt("retry_count"));
        email.setLastError(rs.getString("last_error"));
        email.setScheduledAt(rs.getTimestamp("scheduled_at"));
        email.setSentAt(rs.getTimestamp("sent_at"));
        return email;
    }

    private Timestamp toTimestamp(Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
