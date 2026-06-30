package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.item.SessionItem;

import java.math.BigDecimal;
import java.util.List;

public class ReceiptSessionData {

    private String sessionId;
    private String receiptId;
    private String receiptUrl;
    private String status;
    private List<SessionItem> items;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<SessionItem> getItems() { return items; }
    public void setItems(List<SessionItem> items) { this.items = items; }


}
