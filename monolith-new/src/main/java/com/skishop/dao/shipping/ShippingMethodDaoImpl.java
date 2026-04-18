package com.skishop.dao.shipping;

import com.skishop.domain.shipping.ShippingMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ShippingMethodDaoImpl implements ShippingMethodDao {

    private final JdbcTemplate jdbc;

    public ShippingMethodDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ShippingMethod> listActive() {
        return jdbc.query(
                "SELECT id, code, name, fee, is_active, sort_order FROM shipping_methods WHERE is_active = TRUE ORDER BY sort_order",
                (rs, rowNum) -> mapMethod(rs));
    }

    @Override
    public List<ShippingMethod> listAll() {
        return jdbc.query(
                "SELECT id, code, name, fee, is_active, sort_order FROM shipping_methods ORDER BY sort_order",
                (rs, rowNum) -> mapMethod(rs));
    }

    @Override
    public ShippingMethod findByCode(String code) {
        List<ShippingMethod> methods = jdbc.query(
                "SELECT id, code, name, fee, is_active, sort_order FROM shipping_methods WHERE code = ?",
                (rs, rowNum) -> mapMethod(rs), code);
        return methods.isEmpty() ? null : methods.get(0);
    }

    @Override
    public void insert(ShippingMethod method) {
        jdbc.update("INSERT INTO shipping_methods(id, code, name, fee, is_active, sort_order) VALUES(?,?,?,?,?,?)",
                method.getId(), method.getCode(), method.getName(), method.getFee(),
                method.isActive(), method.getSortOrder());
    }

    @Override
    public void update(ShippingMethod method) {
        jdbc.update("UPDATE shipping_methods SET name = ?, fee = ?, is_active = ?, sort_order = ? WHERE code = ?",
                method.getName(), method.getFee(), method.isActive(), method.getSortOrder(), method.getCode());
    }

    private ShippingMethod mapMethod(ResultSet rs) throws SQLException {
        ShippingMethod method = new ShippingMethod();
        method.setId(rs.getString("id"));
        method.setCode(rs.getString("code"));
        method.setName(rs.getString("name"));
        method.setFee(rs.getBigDecimal("fee"));
        method.setActive(rs.getBoolean("is_active"));
        method.setSortOrder(rs.getInt("sort_order"));
        return method;
    }
}
