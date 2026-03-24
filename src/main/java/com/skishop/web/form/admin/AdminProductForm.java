package com.skishop.web.form.admin;

import jakarta.validation.constraints.NotBlank;

public class AdminProductForm {
    private String id;
    @NotBlank private String name;
    private String brand;
    private String description;
    private String categoryId;
    @NotBlank private String price;
    private String status;
    private int inventoryQty;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getInventoryQty() { return inventoryQty; }
    public void setInventoryQty(int inventoryQty) { this.inventoryQty = inventoryQty; }
}
