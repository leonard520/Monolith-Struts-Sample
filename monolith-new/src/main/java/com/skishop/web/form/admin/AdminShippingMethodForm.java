package com.skishop.web.form.admin;

public class AdminShippingMethodForm {
    private String id;
    private String code;
    private String name;
    private String fee;
    private boolean active;
    private int sortOrder;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFee() { return fee; }
    public void setFee(String fee) { this.fee = fee; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
