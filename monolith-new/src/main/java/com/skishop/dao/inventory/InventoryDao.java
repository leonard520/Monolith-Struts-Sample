package com.skishop.dao.inventory;

import com.skishop.domain.inventory.Inventory;

public interface InventoryDao {
    Inventory findByProductId(String productId);
    void insert(Inventory inventory);
    void updateQuantity(String productId, int quantity, String status);
    boolean reserve(String productId, int quantity);
}
