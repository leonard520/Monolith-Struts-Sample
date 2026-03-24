package com.skishop.dao;

import com.skishop.dao.cart.CartDao;
import com.skishop.domain.cart.Cart;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class CartDaoTest extends DaoTestBase {

    @Autowired
    private CartDao cartDao;

    @Test
    void testFindById() {
        Cart cart = cartDao.findById("cart-1");
        assertNotNull(cart);
        assertEquals("u-1", cart.getUserId());
    }
}
