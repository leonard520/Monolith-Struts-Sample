package com.skishop.dao.order;

import com.skishop.domain.order.Return;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReturnDaoImpl implements ReturnDao {

    private final JdbcTemplate jdbc;

    public ReturnDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(Return returnItem) {
        jdbc.update("INSERT INTO returns(id, order_id, order_item_id, reason, quantity, refund_amount, status) VALUES(?,?,?,?,?,?,?)",
                returnItem.getId(), returnItem.getOrderId(), returnItem.getOrderItemId(),
                returnItem.getReason(), returnItem.getQuantity(), returnItem.getRefundAmount(), returnItem.getStatus());
    }

    @Override
    public List<Return> listByOrderId(String orderId) {
        return jdbc.query(
                "SELECT id, order_id, order_item_id, reason, quantity, refund_amount, status FROM returns WHERE order_id = ? ORDER BY id",
                (rs, rowNum) -> {
                    Return returnItem = new Return();
                    returnItem.setId(rs.getString("id"));
                    returnItem.setOrderId(rs.getString("order_id"));
                    returnItem.setOrderItemId(rs.getString("order_item_id"));
                    returnItem.setReason(rs.getString("reason"));
                    returnItem.setQuantity(rs.getInt("quantity"));
                    returnItem.setRefundAmount(rs.getBigDecimal("refund_amount"));
                    returnItem.setStatus(rs.getString("status"));
                    return returnItem;
                }, orderId);
    }
}
