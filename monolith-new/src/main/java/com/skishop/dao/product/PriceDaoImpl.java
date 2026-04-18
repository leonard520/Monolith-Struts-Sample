package com.skishop.dao.product;

import com.skishop.domain.product.Price;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class PriceDaoImpl implements PriceDao {

    private final JdbcTemplate jdbc;

    public PriceDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Price findByProductId(String productId) {
        List<Price> prices = jdbc.query(
                "SELECT id, product_id, regular_price, sale_price, currency_code, sale_start_date, sale_end_date FROM prices WHERE product_id = ?",
                (rs, rowNum) -> mapPrice(rs), productId);
        return prices.isEmpty() ? null : prices.get(0);
    }

    @Override
    public void saveOrUpdate(Price price) {
        Price existing = findByProductId(price.getProductId());
        if (existing == null) {
            jdbc.update("INSERT INTO prices(id, product_id, regular_price, sale_price, currency_code, sale_start_date, sale_end_date) VALUES(?,?,?,?,?,?,?)",
                    price.getId(), price.getProductId(), price.getRegularPrice(), price.getSalePrice(),
                    price.getCurrencyCode(), toTimestamp(price.getSaleStartDate()), toTimestamp(price.getSaleEndDate()));
        } else {
            jdbc.update("UPDATE prices SET regular_price = ?, sale_price = ?, currency_code = ?, sale_start_date = ?, sale_end_date = ? WHERE id = ?",
                    price.getRegularPrice(), price.getSalePrice(), price.getCurrencyCode(),
                    toTimestamp(price.getSaleStartDate()), toTimestamp(price.getSaleEndDate()), existing.getId());
        }
    }

    private Price mapPrice(ResultSet rs) throws SQLException {
        Price price = new Price();
        price.setId(rs.getString("id"));
        price.setProductId(rs.getString("product_id"));
        price.setRegularPrice(rs.getBigDecimal("regular_price"));
        price.setSalePrice(rs.getBigDecimal("sale_price"));
        price.setCurrencyCode(rs.getString("currency_code"));
        price.setSaleStartDate(rs.getTimestamp("sale_start_date"));
        price.setSaleEndDate(rs.getTimestamp("sale_end_date"));
        return price;
    }

    private Timestamp toTimestamp(java.util.Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
