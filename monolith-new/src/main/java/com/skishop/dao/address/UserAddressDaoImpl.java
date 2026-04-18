package com.skishop.dao.address;

import com.skishop.domain.address.Address;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class UserAddressDaoImpl implements UserAddressDao {

    private final JdbcTemplate jdbc;

    public UserAddressDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Address> listByUserId(String userId) {
        return jdbc.query(
                "SELECT id, user_id, label, recipient_name, postal_code, prefecture, address1, address2, phone, is_default, created_at, updated_at FROM user_addresses WHERE user_id = ? ORDER BY is_default DESC, created_at",
                (rs, rowNum) -> mapAddress(rs), userId);
    }

    @Override
    public void save(Address address) {
        if (address.isDefault()) {
            jdbc.update("UPDATE user_addresses SET is_default = FALSE WHERE user_id = ?", address.getUserId());
        }
        jdbc.update("INSERT INTO user_addresses(id, user_id, label, recipient_name, postal_code, prefecture, address1, address2, phone, is_default, created_at, updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                address.getId(), address.getUserId(), address.getLabel(), address.getRecipientName(),
                address.getPostalCode(), address.getPrefecture(), address.getAddress1(), address.getAddress2(),
                address.getPhone(), address.isDefault(), toTimestamp(address.getCreatedAt()), toTimestamp(address.getUpdatedAt()));
    }

    private Address mapAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setId(rs.getString("id"));
        address.setUserId(rs.getString("user_id"));
        address.setLabel(rs.getString("label"));
        address.setRecipientName(rs.getString("recipient_name"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setPrefecture(rs.getString("prefecture"));
        address.setAddress1(rs.getString("address1"));
        address.setAddress2(rs.getString("address2"));
        address.setPhone(rs.getString("phone"));
        address.setDefault(rs.getBoolean("is_default"));
        address.setCreatedAt(rs.getTimestamp("created_at"));
        address.setUpdatedAt(rs.getTimestamp("updated_at"));
        return address;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
