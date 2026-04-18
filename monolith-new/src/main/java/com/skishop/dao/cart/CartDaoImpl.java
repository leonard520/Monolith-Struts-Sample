package com.skishop.dao.cart;

import com.skishop.domain.cart.Cart;
import com.skishop.domain.cart.CartItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class CartDaoImpl implements CartDao {

    private final JdbcTemplate jdbc;

    public CartDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Cart findById(String id) {
        List<Cart> carts = jdbc.query(
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
        return carts.isEmpty() ? null : carts.get(0);
    }

    @Override
    public void insert(Cart cart) {
        jdbc.update("INSERT INTO carts(id, user_id, session_id, status, expires_at) VALUES(?,?,?,?,?)",
                cart.getId(), cart.getUserId(), cart.getSessionId(), cart.getStatus(), toTimestamp(cart.getExpiresAt()));
    }

    @Override
    public void addItem(CartItem item) {
        jdbc.update("INSERT INTO cart_items(id, cart_id, product_id, quantity, unit_price) VALUES(?,?,?,?,?)",
                item.getId(), item.getCartId(), item.getProductId(), item.getQuantity(), item.getUnitPrice());
    }

    @Override
    public List<CartItem> findItemsByCartId(String cartId) {
        return jdbc.query(
                "SELECT ci.id, ci.cart_id, ci.product_id, ci.quantity, ci.unit_price, p.name AS product_name "
                        + "FROM cart_items ci LEFT JOIN products p ON ci.product_id = p.id "
                        + "WHERE ci.cart_id = ? ORDER BY ci.id",
                (rs, rowNum) -> mapCartItem(rs), cartId);
    }

    @Override
    public void updateItemQuantity(String itemId, int quantity) {
        jdbc.update("UPDATE cart_items SET quantity = ? WHERE id = ?", quantity, itemId);
    }

    @Override
    public void deleteItemsByCartId(String cartId) {
        jdbc.update("DELETE FROM cart_items WHERE cart_id = ?", cartId);
    }

    @Override
    public void updateStatus(String cartId, String status) {
        jdbc.update("UPDATE carts SET status = ? WHERE id = ?", status, cartId);
    }

    private CartItem mapCartItem(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setId(rs.getString("id"));
        item.setCartId(rs.getString("cart_id"));
        item.setProductId(rs.getString("product_id"));
        try { item.setProductName(rs.getString("product_name")); } catch (SQLException ignore) {}
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        return item;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
