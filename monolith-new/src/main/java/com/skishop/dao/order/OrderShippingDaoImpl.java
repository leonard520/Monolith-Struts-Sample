package com.skishop.dao.order;

import com.skishop.domain.order.OrderShipping;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class OrderShippingDaoImpl implements OrderShippingDao {

    private final JdbcTemplate jdbc;

    public OrderShippingDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(OrderShipping shipping) {
        jdbc.update("INSERT INTO order_shipping(id, order_id, recipient_name, postal_code, prefecture, address1, address2, phone, shipping_method_code, shipping_fee, requested_delivery_date) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                shipping.getId(), shipping.getOrderId(), shipping.getRecipientName(), shipping.getPostalCode(),
                shipping.getPrefecture(), shipping.getAddress1(), shipping.getAddress2(), shipping.getPhone(),
                shipping.getShippingMethodCode(), shipping.getShippingFee(), toTimestamp(shipping.getRequestedDeliveryDate()));
    }

    @Override
    public OrderShipping findByOrderId(String orderId) {
        List<OrderShipping> list = jdbc.query(
                "SELECT id, order_id, recipient_name, postal_code, prefecture, address1, address2, phone, shipping_method_code, shipping_fee, requested_delivery_date FROM order_shipping WHERE order_id = ?",
                (rs, rowNum) -> mapShipping(rs), orderId);
        return list.isEmpty() ? null : list.get(0);
    }

    private OrderShipping mapShipping(ResultSet rs) throws SQLException {
        OrderShipping shipping = new OrderShipping();
        shipping.setId(rs.getString("id"));
        shipping.setOrderId(rs.getString("order_id"));
        shipping.setRecipientName(rs.getString("recipient_name"));
        shipping.setPostalCode(rs.getString("postal_code"));
        shipping.setPrefecture(rs.getString("prefecture"));
        shipping.setAddress1(rs.getString("address1"));
        shipping.setAddress2(rs.getString("address2"));
        shipping.setPhone(rs.getString("phone"));
        shipping.setShippingMethodCode(rs.getString("shipping_method_code"));
        shipping.setShippingFee(rs.getBigDecimal("shipping_fee"));
        shipping.setRequestedDeliveryDate(rs.getTimestamp("requested_delivery_date"));
        return shipping;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
