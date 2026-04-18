package com.skishop.dao.product;

import com.skishop.domain.product.Price;

public interface PriceDao {
    Price findByProductId(String productId);
    void saveOrUpdate(Price price);
}
