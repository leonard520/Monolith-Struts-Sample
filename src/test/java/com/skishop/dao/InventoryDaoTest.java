package com.skishop.dao;

import com.skishop.dao.inventory.InventoryDao;
import com.skishop.domain.inventory.Inventory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class InventoryDaoTest extends DaoTestBase {

    @Autowired
    private InventoryDao inventoryDao;

    @Test
    void testFindByProductId() {
        Inventory inv = inventoryDao.findByProductId("P001");
        assertNotNull(inv);
    }
}
