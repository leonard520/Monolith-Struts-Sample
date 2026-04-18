package com.skishop.dao.order;

import com.skishop.domain.order.Order;
import com.skishop.domain.order.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class OrderDaoImpl implements OrderDao {

    private final JdbcTemplate jdbc;

    public OrderDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Order findById(String id) {
        List<Order> orders = jdbc.query(
                "SELECT id, order_number, user_id, status, payment_status, subtotal, tax, shipping_fee, discount_amount, total_amount, coupon_code, used_points, created_at, updated_at FROM orders WHERE id = ?",
                (rs, rowNum) -> mapOrder(rs), id);
        return orders.isEmpty() ? null : orders.get(0);
    }

    @Override
    public List<Order> listByUserId(String userId) {
        return jdbc.query(
                "SELECT id, order_number, user_id, status, payment_status, subtotal, tax, shipping_fee, discount_amount, total_amount, coupon_code, used_points, created_at, updated_at FROM orders WHERE user_id = ? ORDER BY created_at DESC",
                (rs, rowNum) -> mapOrder(rs), userId);
    }

    @Override
    public List<Order> listAll(int limit) {
        return jdbc.query(
                "SELECT id, order_number, user_id, status, payment_status, subtotal, tax, shipping_fee, discount_amount, total_amount, coupon_code, used_points, created_at, updated_at FROM orders ORDER BY created_at DESC LIMIT ?",
                (rs, rowNum) -> mapOrder(rs), limit);
    }

    @Override
    public void insertOrder(Order order) {
        jdbc.update("INSERT INTO orders(id, order_number, user_id, status, payment_status, subtotal, tax, shipping_fee, discount_amount, total_amount, coupon_code, used_points, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                order.getId(), order.getOrderNumber(), order.getUserId(), order.getStatus(), order.getPaymentStatus(),
                order.getSubtotal(), order.getTax(), order.getShippingFee(), order.getDiscountAmount(), order.getTotalAmount(),
                order.getCouponCode(), order.getUsedPoints(), toTimestamp(order.getCreatedAt()), toTimestamp(order.getUpdatedAt()));
    }

    @Override
    public void insertOrderItem(OrderItem item) {
        jdbc.update("INSERT INTO order_items(id, order_id, product_id, product_name, sku, unit_price, quantity, subtotal) VALUES(?,?,?,?,?,?,?,?)",
                item.getId(), item.getOrderId(), item.getProductId(), item.getProductName(),
                item.getSku(), item.getUnitPrice(), item.getQuantity(), item.getSubtotal());
    }

    @Override
    public List<OrderItem> listItemsByOrderId(String orderId) {
        return jdbc.query(
                "SELECT id, order_id, product_id, product_name, sku, unit_price, quantity, subtotal FROM order_items WHERE order_id = ? ORDER BY id",
                (rs, rowNum) -> {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getString("id"));
                    item.setOrderId(rs.getString("order_id"));
                    item.setProductId(rs.getString("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setSku(rs.getString("sku"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    return item;
                }, orderId);
    }

    @Override
    public void updateStatus(String orderId, String status) {
        jdbc.update("UPDATE orders SET status = ?, updated_at = ? WHERE id = ?",
                status, new Timestamp(System.currentTimeMillis()), orderId);
    }

    @Override
    public void updatePaymentStatus(String orderId, String paymentStatus) {
        jdbc.update("UPDATE orders SET payment_status = ?, updated_at = ? WHERE id = ?",
                paymentStatus, new Timestamp(System.currentTimeMillis()), orderId);
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getString("id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setUserId(rs.getString("user_id"));
        order.setStatus(rs.getString("status"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setSubtotal(rs.getBigDecimal("subtotal"));
        order.setTax(rs.getBigDecimal("tax"));
        order.setShippingFee(rs.getBigDecimal("shipping_fee"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setCouponCode(rs.getString("coupon_code"));
        order.setUsedPoints(rs.getInt("used_points"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setUpdatedAt(rs.getTimestamp("updated_at"));
        return order;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
