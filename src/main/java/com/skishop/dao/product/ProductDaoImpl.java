package com.skishop.dao.product;

import com.skishop.domain.product.Product;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductDaoImpl implements ProductDao {
    private final JdbcTemplate jdbcTemplate;

    public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Product findById(String id) {
        var results = jdbcTemplate.query(
            "SELECT p.id, p.name, p.brand, p.description, p.category_id, p.sku, p.status, p.created_at, p.updated_at, pr.regular_price, pr.sale_price, pr.currency_code FROM products p LEFT JOIN prices pr ON pr.product_id = p.id WHERE p.id = ?",
            (rs, rowNum) -> mapProduct(rs), id);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Product> findPaged(String keyword, String categoryId, String sort, int offset, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id, p.name, p.brand, p.description, p.category_id, p.sku, p.status, p.created_at, p.updated_at, pr.regular_price, pr.sale_price, pr.currency_code ");
        sql.append("FROM products p LEFT JOIN prices pr ON pr.product_id = p.id LEFT JOIN categories c ON c.id = p.category_id ");
        sql.append("WHERE 1=1 AND p.status = 'ACTIVE'");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.name ILIKE ? OR p.brand ILIKE ? OR p.description ILIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            sql.append(" AND (p.category_id = ? OR c.name ILIKE ?)");
            params.add(categoryId);
            params.add("%" + categoryId + "%");
        }
        if (sort != null) {
            switch (sort) {
                case "priceAsc" -> sql.append(" ORDER BY COALESCE(pr.sale_price, pr.regular_price, 999999999) ASC, p.name");
                case "priceDesc" -> sql.append(" ORDER BY COALESCE(pr.sale_price, pr.regular_price, 0) DESC, p.name");
                case "newest" -> sql.append(" ORDER BY p.created_at DESC");
                default -> sql.append(" ORDER BY p.name");
            }
        } else {
            sql.append(" ORDER BY p.name");
        }
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapProduct(rs), params.toArray());
    }

    public void insert(Product product) {
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        jdbcTemplate.update(
            "INSERT INTO products(id, name, brand, description, category_id, sku, status, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?)",
            product.getId(), product.getName(), product.getBrand(), product.getDescription(),
            product.getCategoryId(), product.getSku(), product.getStatus(),
            product.getCreatedAt() != null ? new java.sql.Timestamp(product.getCreatedAt().getTime()) : now,
            product.getUpdatedAt() != null ? new java.sql.Timestamp(product.getUpdatedAt().getTime()) : now);
    }

    public void update(Product product) {
        jdbcTemplate.update(
            "UPDATE products SET name = ?, brand = ?, description = ?, category_id = ?, sku = ?, status = ?, updated_at = ? WHERE id = ?",
            product.getName(), product.getBrand(), product.getDescription(), product.getCategoryId(),
            product.getSku(), product.getStatus(), new java.sql.Timestamp(System.currentTimeMillis()), product.getId());
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getString("id"));
        product.setName(rs.getString("name"));
        product.setBrand(rs.getString("brand"));
        product.setDescription(rs.getString("description"));
        product.setCategoryId(rs.getString("category_id"));
        product.setSku(rs.getString("sku"));
        product.setStatus(rs.getString("status"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setUpdatedAt(rs.getTimestamp("updated_at"));
        try { product.setRegularPrice(rs.getBigDecimal("regular_price")); } catch (SQLException ignore) {}
        try { product.setSalePrice(rs.getBigDecimal("sale_price")); } catch (SQLException ignore) {}
        try { product.setCurrencyCode(rs.getString("currency_code")); } catch (SQLException ignore) {}
        return product;
    }
}
