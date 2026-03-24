package com.skishop.dao.shipping;

import com.skishop.domain.shipping.ShippingMethod;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ShippingMethodDaoImpl implements ShippingMethodDao {
    private final JdbcTemplate jdbcTemplate;

    public ShippingMethodDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ShippingMethod> listActive() {
        return jdbcTemplate.query(
            "SELECT id, code, name, fee, is_active, sort_order FROM shipping_methods WHERE is_active = TRUE ORDER BY sort_order",
            (rs, rowNum) -> mapMethod(rs));
    }

    public List<ShippingMethod> listAll() {
        return jdbcTemplate.query(
            "SELECT id, code, name, fee, is_active, sort_order FROM shipping_methods ORDER BY sort_order",
            (rs, rowNum) -> mapMethod(rs));
    }

    public ShippingMethod findByCode(String code) {
        var results = jdbcTemplate.query(
            "SELECT id, code, name, fee, is_active, sort_order FROM shipping_methods WHERE code = ?",
            (rs, rowNum) -> mapMethod(rs), code);
        return results.isEmpty() ? null : results.get(0);
    }

    public void insert(ShippingMethod method) {
        jdbcTemplate.update(
            "INSERT INTO shipping_methods(id, code, name, fee, is_active, sort_order) VALUES(?,?,?,?,?,?)",
            method.getId(), method.getCode(), method.getName(), method.getFee(), method.isActive(), method.getSortOrder());
    }

    public void update(ShippingMethod method) {
        jdbcTemplate.update(
            "UPDATE shipping_methods SET name = ?, fee = ?, is_active = ?, sort_order = ? WHERE code = ?",
            method.getName(), method.getFee(), method.isActive(), method.getSortOrder(), method.getCode());
    }

    private ShippingMethod mapMethod(java.sql.ResultSet rs) throws java.sql.SQLException {
        ShippingMethod m = new ShippingMethod();
        m.setId(rs.getString("id"));
        m.setCode(rs.getString("code"));
        m.setName(rs.getString("name"));
        m.setFee(rs.getBigDecimal("fee"));
        m.setActive(rs.getBoolean("is_active"));
        m.setSortOrder(rs.getInt("sort_order"));
        return m;
    }
}
