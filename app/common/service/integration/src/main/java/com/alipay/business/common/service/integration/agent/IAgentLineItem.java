package com.alipay.business.common.service.integration.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IAgentLineItem {
    private String name;
    private int quantity;
    @JsonProperty("unit_price")
    private double unitPrice;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}
