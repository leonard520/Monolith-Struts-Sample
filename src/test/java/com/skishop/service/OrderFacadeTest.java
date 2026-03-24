package com.skishop.service;

import com.skishop.dao.coupon.CouponDao;
import com.skishop.dao.coupon.CouponUsageDao;
import com.skishop.dao.inventory.InventoryDao;
import com.skishop.dao.order.OrderDao;
import com.skishop.dao.order.ReturnDao;
import com.skishop.dao.point.PointAccountDao;
import com.skishop.domain.coupon.Coupon;
import com.skishop.domain.coupon.CouponUsage;
import com.skishop.domain.inventory.Inventory;
import com.skishop.domain.order.Order;
import com.skishop.domain.point.PointAccount;
import com.skishop.service.order.OrderFacade;
import com.skishop.service.payment.PaymentInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderFacadeTest {

    @Autowired private OrderFacade orderFacade;
    @Autowired private CouponDao couponDao;
    @Autowired private CouponUsageDao couponUsageDao;
    @Autowired private PointAccountDao pointAccountDao;
    @Autowired private InventoryDao inventoryDao;
    @Autowired private OrderDao orderDao;
    @Autowired private ReturnDao returnDao;
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
        updateCouponExpiry("SAVE10");
        updatePointExpiry();
    }

    @Test
    void testCheckoutWithCouponAndPoints() {
        PaymentInfo paymentInfo = createPaymentInfo();
        Order order = orderFacade.placeOrder("cart-1", "SAVE10", 100, paymentInfo, "u-1");
        assertNotNull(order);
        BigDecimal subtotal = new BigDecimal("50000");
        BigDecimal discount = new BigDecimal("5000");
        BigDecimal taxable = subtotal.subtract(discount).subtract(new BigDecimal("100"));
        BigDecimal tax = taxable.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = taxable.add(tax);
        assertEquals(0, order.getTotalAmount().compareTo(expectedTotal));
        assertEquals(0, order.getTax().compareTo(tax));
        assertEquals(0, order.getShippingFee().compareTo(BigDecimal.ZERO));
        assertEquals(0, order.getDiscountAmount().compareTo(discount));
        assertEquals(100, order.getUsedPoints());

        Coupon coupon = couponDao.findByCode("SAVE10");
        assertEquals(1, coupon.getUsedCount());
        CouponUsage usage = couponUsageDao.findByOrderId(order.getId());
        assertNotNull(usage);

        PointAccount account = pointAccountDao.findByUserId("u-1");
        assertEquals(493, account.getBalance());

        Inventory inventory = inventoryDao.findByProductId("P001");
        assertEquals(1, inventory.getReservedQuantity());
    }

    @Test
    void testCancelOrder() {
        PaymentInfo paymentInfo = createPaymentInfo();
        Order order = orderFacade.placeOrder("cart-1", "SAVE10", 100, paymentInfo, "u-1");
        Order cancelled = orderFacade.cancelOrder(order.getId(), "u-1");

        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals("VOID", cancelled.getPaymentStatus());

        Coupon coupon = couponDao.findByCode("SAVE10");
        assertEquals(0, coupon.getUsedCount());
        assertNull(couponUsageDao.findByOrderId(order.getId()));

        PointAccount account = pointAccountDao.findByUserId("u-1");
        assertEquals(100, account.getBalance());

        Inventory inventory = inventoryDao.findByProductId("P001");
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    void testReturnOrder() {
        PaymentInfo paymentInfo = createPaymentInfo();
        Order order = orderFacade.placeOrder("cart-1", "SAVE10", 100, paymentInfo, "u-1");
        orderDao.updateStatus(order.getId(), "DELIVERED");

        Order returned = orderFacade.returnOrder(order.getId(), "u-1");
        assertEquals("RETURNED", returned.getStatus());
        assertEquals("REFUNDED", returned.getPaymentStatus());

        Coupon coupon = couponDao.findByCode("SAVE10");
        assertEquals(0, coupon.getUsedCount());

        PointAccount account = pointAccountDao.findByUserId("u-1");
        assertEquals(100, account.getBalance());

        Inventory inventory = inventoryDao.findByProductId("P001");
        assertEquals(0, inventory.getReservedQuantity());
    }

    private PaymentInfo createPaymentInfo() {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setMethod("CARD");
        paymentInfo.setCardNumber("4111111111111111");
        paymentInfo.setCardExpMonth("12");
        paymentInfo.setCardExpYear("2030");
        paymentInfo.setCardCvv("123");
        paymentInfo.setBillingZip("160-0022");
        return paymentInfo;
    }

    private void updateCouponExpiry(String code) throws Exception {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE coupons SET expires_at = ? WHERE code = ?")) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            ps.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
            ps.setString(2, code);
            ps.executeUpdate();
        }
    }

    private void updatePointExpiry() throws Exception {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE point_transactions SET expires_at = ? WHERE expires_at IS NOT NULL")) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 30);
            ps.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
            ps.executeUpdate();
        }
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
