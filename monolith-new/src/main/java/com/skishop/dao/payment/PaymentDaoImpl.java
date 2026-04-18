package com.skishop.dao.payment;

import com.skishop.domain.payment.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class PaymentDaoImpl implements PaymentDao {

    private final JdbcTemplate jdbc;

    public PaymentDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(Payment payment) {
        jdbc.update("INSERT INTO payments(id, order_id, cart_id, amount, currency, status, payment_intent_id, created_at) VALUES(?,?,?,?,?,?,?,?)",
                payment.getId(), payment.getOrderId(), payment.getCartId(), payment.getAmount(),
                payment.getCurrency(), payment.getStatus(), payment.getPaymentIntentId(), toTimestamp(payment.getCreatedAt()));
    }

    @Override
    public Payment findByOrderId(String orderId) {
        List<Payment> payments = jdbc.query(
                "SELECT id, order_id, cart_id, amount, currency, status, payment_intent_id, created_at FROM payments WHERE order_id = ?",
                (rs, rowNum) -> mapPayment(rs), orderId);
        return payments.isEmpty() ? null : payments.get(0);
    }

    @Override
    public void updateStatus(String paymentId, String status) {
        jdbc.update("UPDATE payments SET status = ? WHERE id = ?", status, paymentId);
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getString("id"));
        payment.setOrderId(rs.getString("order_id"));
        payment.setCartId(rs.getString("cart_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setCurrency(rs.getString("currency"));
        payment.setStatus(rs.getString("status"));
        payment.setPaymentIntentId(rs.getString("payment_intent_id"));
        payment.setCreatedAt(rs.getTimestamp("created_at"));
        return payment;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
