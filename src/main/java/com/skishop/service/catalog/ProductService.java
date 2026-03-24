package com.skishop.service.catalog;

import com.skishop.dao.product.ProductDao;
import com.skishop.domain.product.Product;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String STATUS_OUT_OF_STOCK = "OUT_OF_STOCK";
    private final ProductDao productDao;
    private final JdbcTemplate jdbcTemplate;

    public ProductService(ProductDao productDao, JdbcTemplate jdbcTemplate) {
        this.productDao = productDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Product findById(String productId) {
        return productDao.findById(productId);
    }

    public List<Product> search(String keyword, String categoryId, String sort, int offset, int limit) {
        return productDao.findPaged(keyword, categoryId, sort, offset, limit);
    }

    public List<Product> search(String keyword, String categoryId, int offset, int limit) {
        return productDao.findPaged(keyword, categoryId, null, offset, limit);
    }

    @Transactional
    public void deactivateProduct(String productId) {
        jdbcTemplate.update("UPDATE products SET status = ?, updated_at = ? WHERE id = ?",
            STATUS_INACTIVE, new Timestamp(System.currentTimeMillis()), productId);
        jdbcTemplate.update("UPDATE inventory SET quantity = ?, status = ? WHERE product_id = ?",
            0, STATUS_OUT_OF_STOCK, productId);
    }
}
