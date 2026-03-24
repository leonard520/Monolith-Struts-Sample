package com.skishop.service.point;

import com.skishop.dao.point.PointAccountDao;
import com.skishop.dao.point.PointTransactionDao;
import com.skishop.domain.point.PointAccount;
import com.skishop.domain.point.PointTransaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointService {
    private final PointAccountDao pointAccountDao;
    private final PointTransactionDao pointTransactionDao;
    private final JdbcTemplate jdbcTemplate;

    public PointService(PointAccountDao pointAccountDao, PointTransactionDao pointTransactionDao, JdbcTemplate jdbcTemplate) {
        this.pointAccountDao = pointAccountDao;
        this.pointTransactionDao = pointTransactionDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int awardPoints(String userId, String referenceId, BigDecimal totalAmount) {
        if (userId == null || totalAmount == null) return 0;
        int points = calculateAwardPoints(totalAmount);
        if (points <= 0) return 0;
        ensureAccount(userId);
        pointAccountDao.increment(userId, points);
        PointTransaction t = new PointTransaction();
        t.setId(UUID.randomUUID().toString());
        t.setUserId(userId);
        t.setType("EARN");
        t.setAmount(points);
        t.setReferenceId(referenceId);
        t.setDescription("Order points");
        t.setExpiresAt(addDays(new Date(), 365));
        t.setExpired(false);
        t.setCreatedAt(new Date());
        pointTransactionDao.insert(t);
        return points;
    }

    @Transactional
    public void redeemPoints(String userId, int points, String referenceId) {
        if (points <= 0) return;
        expirePoints(userId);
        PointAccount account = ensureAccount(userId);
        if (account.getBalance() < points) throw new IllegalArgumentException("Insufficient points");
        pointAccountDao.increment(userId, -points);
        pointTransactionDao.insert(buildTransaction(userId, "REDEEM", -points, referenceId, "Redeem points"));
    }

    @Transactional
    public void refundPoints(String userId, int points, String referenceId) {
        if (points <= 0) return;
        ensureAccount(userId);
        pointAccountDao.increment(userId, points);
        pointTransactionDao.insert(buildTransaction(userId, "REFUND", points, referenceId, "Refund points"));
    }

    @Transactional
    public void revokePoints(String userId, int points, String referenceId) {
        if (points <= 0) return;
        ensureAccount(userId);
        pointAccountDao.increment(userId, -points);
        pointTransactionDao.insert(buildTransaction(userId, "REVOKE", -points, referenceId, "Revoke points"));
    }

    public int calculateAwardPoints(BigDecimal totalAmount) {
        if (totalAmount == null) return 0;
        return totalAmount.multiply(new BigDecimal("0.01")).setScale(0, RoundingMode.DOWN).intValue();
    }

    public PointAccount getAccount(String userId) {
        expirePoints(userId);
        return ensureAccount(userId);
    }

    private PointAccount ensureAccount(String userId) {
        PointAccount account = pointAccountDao.findByUserId(userId);
        if (account == null) {
            account = new PointAccount();
            account.setId(UUID.randomUUID().toString());
            account.setUserId(userId);
            account.setBalance(0);
            account.setLifetimeEarned(0);
            account.setLifetimeRedeemed(0);
            pointAccountDao.insert(account);
        }
        return account;
    }

    private PointTransaction buildTransaction(String userId, String type, int amount, String referenceId, String description) {
        PointTransaction t = new PointTransaction();
        t.setId(UUID.randomUUID().toString());
        t.setUserId(userId);
        t.setType(type);
        t.setAmount(amount);
        t.setReferenceId(referenceId);
        t.setDescription(description);
        t.setExpiresAt(null);
        t.setExpired(false);
        t.setCreatedAt(new Date());
        return t;
    }

    @Transactional
    public void expirePoints(String userId) {
        if (userId == null) return;
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        Integer expiredAmount = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0) FROM point_transactions WHERE user_id = ? AND is_expired = FALSE AND expires_at IS NOT NULL AND expires_at < ?",
            Integer.class, userId, now);
        if (expiredAmount != null && expiredAmount > 0) {
            jdbcTemplate.update(
                "UPDATE point_transactions SET is_expired = TRUE WHERE user_id = ? AND is_expired = FALSE AND expires_at IS NOT NULL AND expires_at < ?",
                userId, now);
            jdbcTemplate.update("UPDATE point_accounts SET balance = balance - ? WHERE user_id = ?",
                expiredAmount, userId);
        }
    }

    private Date addDays(Date base, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }
}
