package com.skishop.service.cart;

import com.skishop.dao.cart.CartDao;
import com.skishop.dao.product.PriceDao;
import com.skishop.domain.cart.Cart;
import com.skishop.domain.cart.CartItem;
import com.skishop.domain.product.Price;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    private final CartDao cartDao;
    private final PriceDao priceDao;

    public CartService(CartDao cartDao, PriceDao priceDao) {
        this.cartDao = cartDao;
        this.priceDao = priceDao;
    }

    public Cart getCart(String cartId) {
        return cartDao.findById(cartId);
    }

    public Cart createCart(String userId, String sessionId) {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID().toString());
        cart.setUserId(userId);
        cart.setSessionId(sessionId);
        cart.setStatus("ACTIVE");
        cart.setExpiresAt(addDays(new Date(), 30));
        cartDao.insert(cart);
        return cart;
    }

    public List<CartItem> getItems(String cartId) {
        return cartDao.findItemsByCartId(cartId);
    }

    public void addItem(String cartId, String productId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        List<CartItem> items = cartDao.findItemsByCartId(cartId);
        if (items != null) {
            for (CartItem item : items) {
                if (productId.equals(item.getProductId())) {
                    cartDao.updateItemQuantity(item.getId(), item.getQuantity() + quantity);
                    return;
                }
            }
        }
        CartItem item = new CartItem();
        item.setId(UUID.randomUUID().toString());
        item.setCartId(cartId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(resolveUnitPrice(productId));
        cartDao.addItem(item);
    }

    public BigDecimal calculateSubtotal(List<CartItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (items == null) {
            return subtotal;
        }
        for (CartItem item : items) {
            BigDecimal line = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            subtotal = subtotal.add(line);
        }
        return subtotal;
    }

    public void clearCart(String cartId) {
        cartDao.deleteItemsByCartId(cartId);
        cartDao.updateStatus(cartId, "CHECKED_OUT");
    }

    private BigDecimal resolveUnitPrice(String productId) {
        Price price = priceDao.findByProductId(productId);
        if (price == null || price.getRegularPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal salePrice = price.getSalePrice();
        if (salePrice != null && isSaleActive(price)) {
            return salePrice;
        }
        return price.getRegularPrice();
    }

    private boolean isSaleActive(Price price) {
        Date now = new Date();
        if (price.getSaleStartDate() != null && price.getSaleStartDate().after(now)) {
            return false;
        }
        if (price.getSaleEndDate() != null && price.getSaleEndDate().before(now)) {
            return false;
        }
        return true;
    }

    private Date addDays(Date base, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }
}
