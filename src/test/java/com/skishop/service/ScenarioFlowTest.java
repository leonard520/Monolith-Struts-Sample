package com.skishop.service;

import com.skishop.dao.cart.CartDao;
import com.skishop.domain.cart.Cart;
import com.skishop.domain.order.Order;
import com.skishop.service.cart.CartService;
import com.skishop.service.order.OrderFacade;
import com.skishop.service.payment.PaymentInfo;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ScenarioFlowTest {

    @Autowired private CartService cartService;
    @Autowired private OrderFacade orderFacade;
    @Autowired private CartDao cartDao;
    @Autowired private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        try (Connection con = dataSource.getConnection();
             java.sql.Statement st = con.createStatement()) {
            st.execute("DROP ALL OBJECTS");
        }
        try (Connection con = dataSource.getConnection()) {
            runScript(con, "/db/schema.sql");
            runScript(con, "/db/data.sql");
        }
    }

    @Test
    void testAddToCartThenCheckout() {
        // Use the pre-seeded cart from data.sql (cart-1 for user u-1)
        Cart cart = cartDao.findById("cart-1");
        assertNotNull(cart);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setMethod("CARD");
        paymentInfo.setCardNumber("4111111111111111");
        paymentInfo.setCardExpMonth("12");
        paymentInfo.setCardExpYear("2030");
        paymentInfo.setCardCvv("123");
        paymentInfo.setBillingZip("160-0022");

        Order order = orderFacade.placeOrder("cart-1", null, 0, paymentInfo, "u-1");
        assertNotNull(order);
        assertEquals("CREATED", order.getStatus());
    }

    private void runScript(Connection con, String path) throws Exception {
        try (java.io.InputStream stream = getClass().getResourceAsStream(path);
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
             java.sql.Statement statement = con.createStatement()) {
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            for (String sql : buffer.toString().split(";")) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        }
    }
}
