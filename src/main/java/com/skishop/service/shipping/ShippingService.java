package com.skishop.service.shipping;

import com.skishop.dao.order.OrderShippingDao;
import com.skishop.domain.order.OrderShipping;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {
    private static final BigDecimal FREE_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal DEFAULT_FEE = new BigDecimal("800");

    private final OrderShippingDao orderShippingDao;

    public ShippingService(OrderShippingDao orderShippingDao) {
        this.orderShippingDao = orderShippingDao;
    }

    public BigDecimal calculateShipping(BigDecimal subtotal) {
        if (subtotal == null) return DEFAULT_FEE;
        return subtotal.compareTo(FREE_THRESHOLD) >= 0 ? BigDecimal.ZERO : DEFAULT_FEE;
    }

    public BigDecimal calculateShippingFee(BigDecimal subtotal) {
        return calculateShipping(subtotal);
    }

    public void saveOrderShipping(OrderShipping shipping) {
        orderShippingDao.insert(shipping);
    }
}
