package com.alipay.business.biz.service.impl.receipt;

import java.util.List;

public class ReceiptUploadResult {

    private String receiptId;
    private String receiptUrl;
    private List<OcrResult.OcrLineItem> items;

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public List<OcrResult.OcrLineItem> getItems() { return items; }
    public void setItems(List<OcrResult.OcrLineItem> items) { this.items = items; }
}
