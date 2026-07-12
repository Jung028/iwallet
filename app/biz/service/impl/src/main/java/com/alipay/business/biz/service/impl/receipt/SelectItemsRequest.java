package com.alipay.business.biz.service.impl.receipt;

import java.util.Map;

public class SelectItemsRequest {
    // itemId -> desired number of units the requesting user wants to claim of that line's quantity
    private Map<String, Integer> itemQuantities;

    public Map<String, Integer> getItemQuantities() { return itemQuantities; }
    public void setItemQuantities(Map<String, Integer> itemQuantities) { this.itemQuantities = itemQuantities; }
}
