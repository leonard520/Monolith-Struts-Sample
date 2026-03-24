package com.skishop.web.form;

import com.skishop.service.payment.PaymentInfo;
import jakarta.validation.constraints.NotBlank;

public class CheckoutForm {
    private String cartId;
    private String couponCode;
    private String paymentMethod;
    @NotBlank private String cardNumber;
    @NotBlank private String cardExpMonth;
    @NotBlank private String cardExpYear;
    @NotBlank private String cardCvv;
    @NotBlank private String billingZip;
    private int usePoints;

    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCardExpMonth() { return cardExpMonth; }
    public void setCardExpMonth(String cardExpMonth) { this.cardExpMonth = cardExpMonth; }
    public String getCardExpYear() { return cardExpYear; }
    public void setCardExpYear(String cardExpYear) { this.cardExpYear = cardExpYear; }
    public String getCardCvv() { return cardCvv; }
    public void setCardCvv(String cardCvv) { this.cardCvv = cardCvv; }
    public String getBillingZip() { return billingZip; }
    public void setBillingZip(String billingZip) { this.billingZip = billingZip; }
    public int getUsePoints() { return usePoints; }
    public void setUsePoints(int usePoints) { this.usePoints = usePoints; }

    public PaymentInfo toPaymentInfo() {
        PaymentInfo info = new PaymentInfo();
        info.setMethod(paymentMethod);
        info.setCardNumber(cardNumber);
        info.setCardExpMonth(cardExpMonth);
        info.setCardExpYear(cardExpYear);
        info.setCardCvv(cardCvv);
        info.setBillingZip(billingZip);
        return info;
    }
}
