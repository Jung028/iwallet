package com.alipay.business.biz.service.impl.event;

import com.alipay.business.core.model.domain.ReceiptItemDomain;

/**
 * @author adam
 * @date 30/6/2026 11:16 AM
 */
public class ReceiptItemPaidEvent {
    private ReceiptItemDomain receiptItem;

    public ReceiptItemPaidEvent(ReceiptItemDomain receiptItem) {
        this.receiptItem = receiptItem;
    }

    public ReceiptItemDomain getReceiptItem() {
        return receiptItem;
    }

    public void setReceiptItem(ReceiptItemDomain receiptItem) {
        this.receiptItem = receiptItem;
    }
}
