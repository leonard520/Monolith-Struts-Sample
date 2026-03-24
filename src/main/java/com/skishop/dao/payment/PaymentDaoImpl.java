package com.skishop.dao.payment;

import com.skishop.domain.payment.Payment;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentDaoImpl implements PaymentDao {
    private final JdbcTemplate jdbcTemplate;

    public PaymentDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Payment payment) {
        jdbcTemplate.update(
            "INSERT INTO payments(id, order_id, cart_id, amount, currency, status, payment_intent_id, created_at) VALUES(?,?,?,?,?,?,?,?)",
            payment.getId(), payment.getOrderId(), payment.getCartId(), payment.getAmount(),
            payment.getCurrency(), payment.getStatus(), payment.getPaymentIntentId(), toTimestamp(payment.getCreatedAt()));
    }

    public Payment findByOrderId(String orderId) {
        var results = jdbcTemplate.query(
            "SELECT id, order_id, cart_id, amount, currency, status, payment_intent_id, created_at FROM payments WHERE order_id = ?",
            (rs, rowNum) -> {
                Payment p = new Payment();
                p.setId(rs.getString("id"));
                p.setOrderId(rs.getString("order_id"));
                p.setCartId(rs.getString("cart_id"));
                p.setAmount(rs.getBigDecimal("amount"));
                p.setCurrency(rs.getString("currency"));
                p.setStatus(rs.getString("status"));
                p.setPaymentIntentId(rs.getString("payment_intent_id"));
                p.setCreatedAt(rs.getTimestamp("created_at"));
                return p;
            }, orderId);
        return results.isEmpty() ? null : results.get(0);
    }

    public void updateStatus(String paymentId, String status) {
        jdbcTemplate.update("UPDATE payments SET status = ? WHERE id = ?", status, paymentId);
    }

    private Timestamp toTimestamp(java.util.Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }
}
