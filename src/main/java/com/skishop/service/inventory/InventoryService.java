package com.skishop.service.inventory;

import com.skishop.domain.cart.CartItem;
import com.skishop.domain.inventory.Inventory;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final JdbcTemplate jdbcTemplate;

    public InventoryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void reserveItems(List<CartItem> items) {
        updateReservation(items, true);
    }

    @Transactional
    public void releaseItems(List<CartItem> items) {
        updateReservation(items, false);
    }

    private void updateReservation(List<CartItem> items, boolean reserve) {
        if (items == null || items.isEmpty()) return;
        for (CartItem item : items) {
            Inventory inventory = lockInventory(item.getProductId());
            if (inventory == null) throw new IllegalStateException("Inventory not found: " + item.getProductId());
            if (reserve) {
                int available = inventory.getQuantity() - inventory.getReservedQuantity();
                if (available < item.getQuantity())
                    throw new IllegalStateException("Insufficient stock: " + item.getProductId());
                jdbcTemplate.update(
                    "UPDATE inventory SET reserved_quantity = reserved_quantity + ? WHERE product_id = ?",
                    item.getQuantity(), item.getProductId());
            } else {
                jdbcTemplate.update(
                    "UPDATE inventory SET reserved_quantity = CASE WHEN reserved_quantity + ? < 0 THEN 0 ELSE reserved_quantity + ? END WHERE product_id = ?",
                    -item.getQuantity(), -item.getQuantity(), item.getProductId());
            }
        }
    }

    private Inventory lockInventory(String productId) {
        var results = jdbcTemplate.query(
            "SELECT id, product_id, quantity, reserved_quantity, status FROM inventory WHERE product_id = ? FOR UPDATE",
            (rs, rowNum) -> {
                Inventory inv = new Inventory();
                inv.setId(rs.getString("id"));
                inv.setProductId(rs.getString("product_id"));
                inv.setQuantity(rs.getInt("quantity"));
                inv.setReservedQuantity(rs.getInt("reserved_quantity"));
                inv.setStatus(rs.getString("status"));
                return inv;
            }, productId);
        return results.isEmpty() ? null : results.get(0);
    }
}
