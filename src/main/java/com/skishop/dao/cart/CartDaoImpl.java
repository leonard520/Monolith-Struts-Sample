package com.skishop.dao.cart;

import com.skishop.domain.cart.Cart;
import com.skishop.domain.cart.CartItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CartDaoImpl implements CartDao {
    private final JdbcTemplate jdbcTemplate;

    public CartDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Cart findById(String id) {
        var results = jdbcTemplate.query(
            "SELECT id, user_id, session_id, status, expires_at FROM carts WHERE id = ?",
            (rs, rowNum) -> {
                Cart cart = new Cart();
                cart.setId(rs.getString("id"));
                cart.setUserId(rs.getString("user_id"));
                cart.setSessionId(rs.getString("session_id"));
                cart.setStatus(rs.getString("status"));
                cart.setExpiresAt(rs.getTimestamp("expires_at"));
                return cart;
            }, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public void insert(Cart cart) {
        jdbcTemplate.update("INSERT INTO carts(id, user_id, session_id, status, expires_at) VALUES(?,?,?,?,?)",
            cart.getId(), cart.getUserId(), cart.getSessionId(), cart.getStatus(), toTimestamp(cart.getExpiresAt()));
    }

    public void addItem(CartItem item) {
        jdbcTemplate.update("INSERT INTO cart_items(id, cart_id, product_id, quantity, unit_price) VALUES(?,?,?,?,?)",
            item.getId(), item.getCartId(), item.getProductId(), item.getQuantity(), item.getUnitPrice());
    }

    public List<CartItem> findItemsByCartId(String cartId) {
        return jdbcTemplate.query(
            "SELECT ci.id, ci.cart_id, ci.product_id, ci.quantity, ci.unit_price, p.name AS product_name " +
            "FROM cart_items ci LEFT JOIN products p ON ci.product_id = p.id WHERE ci.cart_id = ? ORDER BY ci.id",
            (rs, rowNum) -> {
                CartItem item = new CartItem();
                item.setId(rs.getString("id"));
                item.setCartId(rs.getString("cart_id"));
                item.setProductId(rs.getString("product_id"));
                try { item.setProductName(rs.getString("product_name")); } catch (SQLException ignore) {}
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                return item;
            }, cartId);
    }

    public void updateItemQuantity(String itemId, int quantity) {
        jdbcTemplate.update("UPDATE cart_items SET quantity = ? WHERE id = ?", quantity, itemId);
    }

    public void deleteItemsByCartId(String cartId) {
        jdbcTemplate.update("DELETE FROM cart_items WHERE cart_id = ?", cartId);
    }

    public void updateStatus(String cartId, String status) {
        jdbcTemplate.update("UPDATE carts SET status = ? WHERE id = ?", status, cartId);
    }

    private Timestamp toTimestamp(java.util.Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }
}
