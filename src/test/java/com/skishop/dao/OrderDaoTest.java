package com.skishop.dao;

import com.skishop.dao.order.OrderDao;
import com.skishop.domain.order.Order;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class OrderDaoTest extends DaoTestBase {

    @Autowired
    private OrderDao orderDao;

    @Test
    void testInsertAndFindById() {
        Order order = new Order();
        order.setId("o-99");
        order.setOrderNumber("ORD-99");
        order.setUserId("u-1");
        order.setStatus("PENDING");
        order.setPaymentStatus("PENDING");
        order.setSubtotal(BigDecimal.valueOf(10000));
        order.setTotalAmount(BigDecimal.valueOf(10000));
        order.setTax(BigDecimal.valueOf(1000));
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setUsedPoints(0);
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        orderDao.insertOrder(order);

        Order loaded = orderDao.findById("o-99");
        assertNotNull(loaded);
        assertEquals("ORD-99", loaded.getOrderNumber());
    }

    @Test
    void testListByUserId() {
        List<Order> orders = orderDao.listByUserId("u-1");
        assertNotNull(orders);
    }
}
