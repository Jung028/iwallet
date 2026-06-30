package com.alipay.business.biz.service.impl.event;

import com.alipay.business.common.service.facade.item.ReceiptItem;

/**
 * @author adam
 * @date 30/6/2026 11:16 AM
 */
public class ReceiptItemPaidEvent {
    private ReceiptItem receiptItem;

    public ReceiptItemPaidEvent(ReceiptItem receiptItem) {
        this.receiptItem = receiptItem;
    }

    public ReceiptItem getReceiptItem() {
        return receiptItem;
    }

    public void setReceiptItem(ReceiptItem receiptItem) {
        this.receiptItem = receiptItem;
    }
}