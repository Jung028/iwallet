package com.alipay.business.common.service.integration.agent;

import java.math.BigDecimal;
import java.util.List;

public class OcrResult {

    private BigDecimal totalAmount;
    private String currency;
    // sum of every tax/charge line extracted from the receipt (service charge, SST, GST, etc.)
    private BigDecimal totalTaxAmount;
    private List<OcrLineItem> items;

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getTotalTaxAmount() { return totalTaxAmount; }
    public void setTotalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; }
    public List<OcrLineItem> getItems() { return items; }
    public void setItems(List<OcrLineItem> items) { this.items = items; }

    public static class OcrLineItem {
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    }
}
