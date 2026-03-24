package com.skishop.dao.user;

import com.skishop.domain.user.SecurityLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SecurityLogDaoImpl implements SecurityLogDao {
    private final JdbcTemplate jdbcTemplate;

    public SecurityLogDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(SecurityLog log) {
        jdbcTemplate.update(
            "INSERT INTO security_logs(id, user_id, event_type, ip_address, user_agent, details_json) VALUES(?,?,?,?,?,?)",
            log.getId(), log.getUserId(), log.getEventType(), log.getIpAddress(), log.getUserAgent(), log.getDetailsJson());
    }

    public int countByUserAndEvent(String userId, String eventType) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM security_logs WHERE user_id = ? AND event_type = ?",
            Integer.class, userId, eventType);
        return count != null ? count : 0;
    }
}
