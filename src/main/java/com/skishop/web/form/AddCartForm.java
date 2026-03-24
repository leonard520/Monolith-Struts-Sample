package com.skishop.web.form;

import jakarta.validation.constraints.NotBlank;

public class AddCartForm {
    @NotBlank private String productId;
    private int quantity = 1;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
