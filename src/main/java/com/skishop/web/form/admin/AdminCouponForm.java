package com.skishop.web.form.admin;

import jakarta.validation.constraints.NotBlank;

public class AdminCouponForm {
    private String id;
    private String campaignId;
    @NotBlank private String code;
    private String couponType;
    @NotBlank private String discountValue;
    private String discountType;
    private String minimumAmount;
    private String maximumDiscount;
    private int usageLimit;
    private boolean active;
    private String expiresAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCouponType() { return couponType; }
    public void setCouponType(String couponType) { this.couponType = couponType; }
    public String getDiscountValue() { return discountValue; }
    public void setDiscountValue(String discountValue) { this.discountValue = discountValue; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public String getMinimumAmount() { return minimumAmount; }
    public void setMinimumAmount(String minimumAmount) { this.minimumAmount = minimumAmount; }
    public String getMaximumDiscount() { return maximumDiscount; }
    public void setMaximumDiscount(String maximumDiscount) { this.maximumDiscount = maximumDiscount; }
    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
