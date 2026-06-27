package com.alipay.business.biz.service.impl.receipt;

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

    
}
