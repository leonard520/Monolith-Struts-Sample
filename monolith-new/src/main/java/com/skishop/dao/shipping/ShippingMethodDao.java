package com.skishop.dao.shipping;

import com.skishop.domain.shipping.ShippingMethod;
import java.util.List;

public interface ShippingMethodDao {
    List<ShippingMethod> listActive();
    List<ShippingMethod> listAll();
    ShippingMethod findByCode(String code);
    void insert(ShippingMethod method);
    void update(ShippingMethod method);
}
