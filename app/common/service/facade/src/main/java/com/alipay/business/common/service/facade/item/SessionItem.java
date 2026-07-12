package com.alipay.business.common.service.facade.item;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author adam
 * @date 30/6/2026 11:07 PM
 */
public class SessionItem {
    private String itemId;
    private String name;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal taxAmount;
    // userId -> units of this line's quantity claimed by that user; lets a multi-quantity
    // item (e.g. "2x Fried Chicken") be split between several people instead of one owner.
    private Map<String, Integer> claims = new HashMap<>();
    private String status; // AVAILABLE, SELECTED, PAID

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public Map<String, Integer> getClaims() { return claims; }
    public void setClaims(Map<String, Integer> claims) { this.claims = claims; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int totalClaimed() {
        return claims.values().stream().mapToInt(Integer::intValue).sum();
    }
}
