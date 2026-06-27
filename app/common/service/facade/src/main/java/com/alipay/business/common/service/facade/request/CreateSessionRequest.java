package com.alipay.business.common.service.facade.request;

/**
 * @author adam
 * @date 26/6/2026 10:07 PM
 */
public class CreateSessionRequest {
    private String receiptId;
    private String receiptUrl;

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }
}