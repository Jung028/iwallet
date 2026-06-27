package com.alipay.business.common.service.integration.agent;

import java.math.BigDecimal;
import java.util.List;

public class OcrResult {

    private BigDecimal totalAmount;
    private String currency;
    private List<OcrLineItem> items;

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public List<OcrLineItem> getItems() { return items; }
    public void setItems(List<OcrLineItem> items) { this.items = items; }

    public static class OcrLineItem {
        private String name;
        private int quantity;
        private BigDecimal unitPrice;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}
