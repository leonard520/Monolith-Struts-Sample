package com.skishop.dao.order;

import com.skishop.domain.order.Return;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReturnDaoImpl implements ReturnDao {
    private final JdbcTemplate jdbcTemplate;

    public ReturnDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Return returnItem) {
        jdbcTemplate.update(
            "INSERT INTO returns(id, order_id, order_item_id, reason, quantity, refund_amount, status) VALUES(?,?,?,?,?,?,?)",
            returnItem.getId(), returnItem.getOrderId(), returnItem.getOrderItemId(),
            returnItem.getReason(), returnItem.getQuantity(), returnItem.getRefundAmount(), returnItem.getStatus());
    }

    public List<Return> listByOrderId(String orderId) {
        return jdbcTemplate.query(
            "SELECT id, order_id, order_item_id, reason, quantity, refund_amount, status FROM returns WHERE order_id = ? ORDER BY id",
            (rs, rowNum) -> {
                Return r = new Return();
                r.setId(rs.getString("id"));
                r.setOrderId(rs.getString("order_id"));
                r.setOrderItemId(rs.getString("order_item_id"));
                r.setReason(rs.getString("reason"));
                r.setQuantity(rs.getInt("quantity"));
                r.setRefundAmount(rs.getBigDecimal("refund_amount"));
                r.setStatus(rs.getString("status"));
                return r;
            }, orderId);
    }
}
