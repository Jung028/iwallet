package com.alipay.business.biz.service.impl.receipt;

import java.util.List;

public class SelectItemsRequest {
    private List<String> itemIds;

    public List<String> getItemIds() { return itemIds; }
    public void setItemIds(List<String> itemIds) { this.itemIds = itemIds; }
}
