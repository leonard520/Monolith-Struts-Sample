package com.skishop.dao.inventory;

import com.skishop.domain.inventory.Inventory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryDaoImpl implements InventoryDao {
    private final JdbcTemplate jdbcTemplate;

    public InventoryDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Inventory findByProductId(String productId) {
        var results = jdbcTemplate.query(
            "SELECT id, product_id, quantity, reserved_quantity, status FROM inventory WHERE product_id = ?",
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

    public void insert(Inventory inventory) {
        jdbcTemplate.update(
            "INSERT INTO inventory(id, product_id, quantity, reserved_quantity, status) VALUES(?,?,?,?,?)",
            inventory.getId(), inventory.getProductId(), inventory.getQuantity(),
            inventory.getReservedQuantity(), inventory.getStatus());
    }

    public void updateQuantity(String productId, int quantity, String status) {
        jdbcTemplate.update("UPDATE inventory SET quantity = ?, status = ? WHERE product_id = ?",
            quantity, status, productId);
    }

    public boolean reserve(String productId, int quantity) {
        int updated = jdbcTemplate.update(
            "UPDATE inventory SET reserved_quantity = reserved_quantity + ? WHERE product_id = ? AND (quantity - reserved_quantity) >= ?",
            quantity, productId, quantity);
        return updated > 0;
    }
}
