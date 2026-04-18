package com.skishop.dao.category;

import com.skishop.domain.product.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryDaoImpl implements CategoryDao {

    private final JdbcTemplate jdbc;

    public CategoryDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Category> findAll() {
        return jdbc.query("SELECT id, name, parent_id FROM categories ORDER BY name",
                (rs, rowNum) -> {
                    Category c = new Category();
                    c.setId(rs.getString("id"));
                    c.setName(rs.getString("name"));
                    c.setParentId(rs.getString("parent_id"));
                    return c;
                });
    }
}
