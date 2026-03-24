package com.skishop.dao.category;

import com.skishop.domain.product.Category;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryDaoImpl implements CategoryDao {
    private final JdbcTemplate jdbcTemplate;

    public CategoryDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Category> findAll() {
        return jdbcTemplate.query("SELECT id, name, parent_id FROM categories ORDER BY name", (rs, rowNum) -> {
            Category c = new Category();
            c.setId(rs.getString("id"));
            c.setName(rs.getString("name"));
            c.setParentId(rs.getString("parent_id"));
            return c;
        });
    }
}
