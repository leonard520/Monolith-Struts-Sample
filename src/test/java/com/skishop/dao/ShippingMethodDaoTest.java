package com.skishop.dao;

import com.skishop.dao.shipping.ShippingMethodDao;
import com.skishop.domain.shipping.ShippingMethod;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class ShippingMethodDaoTest extends DaoTestBase {

    @Autowired
    private ShippingMethodDao shippingMethodDao;

    @Test
    void testListActive() {
        List<ShippingMethod> methods = shippingMethodDao.listActive();
        assertNotNull(methods);
    }
}
