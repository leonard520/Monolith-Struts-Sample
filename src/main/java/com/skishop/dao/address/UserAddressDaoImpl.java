package com.skishop.dao.address;

import com.skishop.domain.address.Address;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserAddressDaoImpl implements UserAddressDao {
    private final JdbcTemplate jdbcTemplate;

    public UserAddressDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Address> listByUserId(String userId) {
        return jdbcTemplate.query(
            "SELECT id, user_id, label, recipient_name, postal_code, prefecture, address1, address2, phone, is_default, created_at, updated_at FROM user_addresses WHERE user_id = ? ORDER BY is_default DESC, created_at",
            (rs, rowNum) -> {
                Address a = new Address();
                a.setId(rs.getString("id"));
                a.setUserId(rs.getString("user_id"));
                a.setLabel(rs.getString("label"));
                a.setRecipientName(rs.getString("recipient_name"));
                a.setPostalCode(rs.getString("postal_code"));
                a.setPrefecture(rs.getString("prefecture"));
                a.setAddress1(rs.getString("address1"));
                a.setAddress2(rs.getString("address2"));
                a.setPhone(rs.getString("phone"));
                a.setDefault(rs.getBoolean("is_default"));
                a.setCreatedAt(rs.getTimestamp("created_at"));
                a.setUpdatedAt(rs.getTimestamp("updated_at"));
                return a;
            }, userId);
    }

    public Address findById(String id) {
        var results = jdbcTemplate.query(
            "SELECT id, user_id, label, recipient_name, postal_code, prefecture, address1, address2, phone, is_default, created_at, updated_at FROM user_addresses WHERE id = ?",
            (rs, rowNum) -> {
                Address a = new Address();
                a.setId(rs.getString("id"));
                a.setUserId(rs.getString("user_id"));
                a.setLabel(rs.getString("label"));
                a.setRecipientName(rs.getString("recipient_name"));
                a.setPostalCode(rs.getString("postal_code"));
                a.setPrefecture(rs.getString("prefecture"));
                a.setAddress1(rs.getString("address1"));
                a.setAddress2(rs.getString("address2"));
                a.setPhone(rs.getString("phone"));
                a.setDefault(rs.getBoolean("is_default"));
                a.setCreatedAt(rs.getTimestamp("created_at"));
                a.setUpdatedAt(rs.getTimestamp("updated_at"));
                return a;
            }, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public void save(Address address) {
        if (address.isDefault()) {
            jdbcTemplate.update("UPDATE user_addresses SET is_default = FALSE WHERE user_id = ?", address.getUserId());
        }
        jdbcTemplate.update(
            "INSERT INTO user_addresses(id, user_id, label, recipient_name, postal_code, prefecture, address1, address2, phone, is_default, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
            address.getId(), address.getUserId(), address.getLabel(), address.getRecipientName(),
            address.getPostalCode(), address.getPrefecture(), address.getAddress1(), address.getAddress2(),
            address.getPhone(), address.isDefault(), toTimestamp(address.getCreatedAt()), toTimestamp(address.getUpdatedAt()));
    }

    private Timestamp toTimestamp(java.util.Date date) {
        if (date == null) return null;
        return new Timestamp(date.getTime());
    }
}
