package com.skishop.dao.order;

import com.skishop.domain.order.OrderShipping;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderShippingDaoImpl implements OrderShippingDao {
    private final JdbcTemplate jdbcTemplate;

    public OrderShippingDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(OrderShipping shipping) {
        jdbcTemplate.update(
            "INSERT INTO order_shipping(id, order_id, recipient_name, postal_code, prefecture, address1, address2, phone, shipping_method_code, shipping_fee, requested_delivery_date) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
            shipping.getId(), shipping.getOrderId(), shipping.getRecipientName(), shipping.getPostalCode(),
            shipping.getPrefecture(), shipping.getAddress1(), shipping.getAddress2(), shipping.getPhone(),
            shipping.getShippingMethodCode(), shipping.getShippingFee(), toTimestamp(shipping.getRequestedDeliveryDate()));
    }

    public OrderShipping findByOrderId(String orderId) {
        var results = jdbcTemplate.query(
            "SELECT id, order_id, recipient_name, postal_code, prefecture, address1, address2, phone, shipping_method_code, shipping_fee, requested_delivery_date FROM order_shipping WHERE order_id = ?",
            (rs, rowNum) -> {
                OrderShipping s = new OrderShipping();
                s.setId(rs.getString("id"));
                s.setOrderId(rs.getString("order_id"));
                s.setRecipientName(rs.getString("recipient_name"));
                s.setPostalCode(rs.getString("postal_code"));
                s.setPrefecture(rs.getString("prefecture"));
                s.setAddress1(rs.getString("address1"));
                s.setAddress2(rs.getString("address2"));
                s.setPhone(rs.getString("phone"));
                s.setShippingMethodCode(rs.getString("shipping_method_code"));
                s.setShippingFee(rs.getBigDecimal("shipping_fee"));
                s.setRequestedDeliveryDate(rs.getTimestamp("requested_delivery_date"));
                return s;
            }, orderId);
        return results.isEmpty() ? null : results.get(0);
    }

    private Timestamp toTimestamp(java.util.Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }
}
