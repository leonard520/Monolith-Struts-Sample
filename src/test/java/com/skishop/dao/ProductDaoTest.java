package com.skishop.dao;

import com.skishop.dao.product.ProductDao;
import com.skishop.domain.product.Product;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class ProductDaoTest extends DaoTestBase {

    @Autowired
    private ProductDao productDao;

    @Test
    void testFindById() {
        Product product = productDao.findById("P001");
        assertNotNull(product);
        assertEquals("Ski A", product.getName());
    }

    @Test
    void testFindPaged() {
        List<Product> products = productDao.findPaged("Ski", "c-1", null, 0, 10);
        assertFalse(products.isEmpty());
    }

    @Test
    void testInsert() {
        Product product = new Product();
        product.setId("P999");
        product.setName("Ski Pro");
        product.setBrand("BrandZ");
        product.setDescription("Pro ski");
        product.setCategoryId("c-1");
        product.setSku("SKU-999");
        product.setStatus("ACTIVE");
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());
        productDao.insert(product);

        Product loaded = productDao.findById("P999");
        assertNotNull(loaded);
        assertEquals("Ski Pro", loaded.getName());
    }
}
