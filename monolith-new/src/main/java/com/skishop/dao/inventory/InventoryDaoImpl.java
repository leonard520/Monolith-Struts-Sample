package com.skishop.dao.inventory;

import com.skishop.domain.inventory.Inventory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class InventoryDaoImpl implements InventoryDao {

    private final JdbcTemplate jdbc;

    public InventoryDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Inventory findByProductId(String productId) {
        List<Inventory> list = jdbc.query(
                "SELECT id, product_id, quantity, reserved_quantity, status FROM inventory WHERE product_id = ?",
                (rs, rowNum) -> mapInventory(rs), productId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void insert(Inventory inventory) {
        jdbc.update("INSERT INTO inventory(id, product_id, quantity, reserved_quantity, status) VALUES(?,?,?,?,?)",
                inventory.getId(), inventory.getProductId(), inventory.getQuantity(),
                inventory.getReservedQuantity(), inventory.getStatus());
    }

    @Override
    public void updateQuantity(String productId, int quantity, String status) {
        jdbc.update("UPDATE inventory SET quantity = ?, status = ? WHERE product_id = ?",
                quantity, status, productId);
    }

    @Override
    public boolean reserve(String productId, int quantity) {
        int rows = jdbc.update(
                "UPDATE inventory SET reserved_quantity = reserved_quantity + ? WHERE product_id = ? AND (quantity - reserved_quantity) >= ?",
                quantity, productId, quantity);
        return rows > 0;
    }

    private Inventory mapInventory(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setId(rs.getString("id"));
        inventory.setProductId(rs.getString("product_id"));
        inventory.setQuantity(rs.getInt("quantity"));
        inventory.setReservedQuantity(rs.getInt("reserved_quantity"));
        inventory.setStatus(rs.getString("status"));
        return inventory;
    }
}
