package com.alipay.business.common.service.facade.item;

import java.math.BigDecimal;

/**
 * @author adam
 * @date 30/6/2026 11:07 PM
 */
public class SessionItem {
    private String itemId;
    private String name;
    private BigDecimal price;
    private String selectedBy;
    private String status; // AVAILABLE, SELECTED, PAID

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getSelectedBy() { return selectedBy; }
    public void setSelectedBy(String selectedBy) { this.selectedBy = selectedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}