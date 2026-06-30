package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;

import java.util.List;

public class ReceiptUploadResult {

    private String receiptId;
    private String receiptUrl;
    private List<ReceiptSubItem> items;

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public List<ReceiptSubItem> getItems() { return items; }
    public void setItems(List<ReceiptSubItem> items) { this.items = items; }
}
