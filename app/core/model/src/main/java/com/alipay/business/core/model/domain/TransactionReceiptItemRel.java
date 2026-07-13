package com.alipay.business.core.model.domain;

import java.util.Date;
import java.util.UUID;

/**
 * @author adam
 * @date 12/7/2026 7:19 PM
 */
public class TransactionReceiptItemRel {
    private String txnId;
    private UUID receiptItemId;
    private Integer receiptItemQuantity;
    private Date gmtCreate;
    private Date gmtModified;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public UUID getReceiptItemId() {
        return receiptItemId;
    }

    public void setReceiptItemId(UUID receiptItemId) {
        this.receiptItemId = receiptItemId;
    }

    public Integer getReceiptItemQuantity() {
        return receiptItemQuantity;
    }

    public void setReceiptItemQuantity(Integer receiptItemQuantity) {
        this.receiptItemQuantity = receiptItemQuantity;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }
}