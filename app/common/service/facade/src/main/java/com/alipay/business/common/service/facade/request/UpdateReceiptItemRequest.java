package com.alipay.business.common.service.facade.request;

import java.util.Date;

/**
 * @author adam
 * @date 29/6/2026 11:57 PM
 */
public class UpdateReceiptItemRequest {
    private String receiptItemId;
    private String itemStatus;
    private String name;
    private Date gmtUpdatedAt;

    public String getReceiptItemId() {
        return receiptItemId;
    }

    public void setReceiptItemId(String receiptItemId) {
        this.receiptItemId = receiptItemId;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getGmtUpdatedAt() {
        return gmtUpdatedAt;
    }

    public void setGmtUpdatedAt(Date gmtUpdatedAt) {
        this.gmtUpdatedAt = gmtUpdatedAt;
    }
}