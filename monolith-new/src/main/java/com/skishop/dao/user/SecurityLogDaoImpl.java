package com.skishop.dao.user;

import com.skishop.domain.user.SecurityLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SecurityLogDaoImpl implements SecurityLogDao {

    private final JdbcTemplate jdbc;

    public SecurityLogDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(SecurityLog log) {
        jdbc.update("INSERT INTO security_logs(id, user_id, event_type, ip_address, user_agent, details_json) VALUES(?,?,?,?,?,?)",
                log.getId(), log.getUserId(), log.getEventType(), log.getIpAddress(), log.getUserAgent(), log.getDetailsJson());
    }

    @Override
    public int countByUserAndEvent(String userId, String eventType) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM security_logs WHERE user_id = ? AND event_type = ?",
                Integer.class, userId, eventType);
        return count != null ? count : 0;
    }
}
