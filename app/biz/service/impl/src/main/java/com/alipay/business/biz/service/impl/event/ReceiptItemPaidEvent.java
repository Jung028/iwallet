package com.alipay.business.biz.service.impl.event;

import com.alipay.business.core.model.domain.ReceiptItemDomain;

import java.util.List;

/**
 * @author adam
 * @date 30/6/2026 11:16 AM
 */
public class ReceiptItemPaidEvent {
    private String receiptId;
    private List<ReceiptItemDomain> receiptItems;

    public ReceiptItemPaidEvent(String receiptId, List<ReceiptItemDomain> receiptItems) {
        this.receiptId = receiptId;
        this.receiptItems = receiptItems;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public List<ReceiptItemDomain> getReceiptItems() {
        return receiptItems;
    }

    public void setReceiptItems(List<ReceiptItemDomain> receiptItems) {
        this.receiptItems = receiptItems;
    }
}
