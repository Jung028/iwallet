package com.alipay.business.common.service.facade.request;

import java.math.BigDecimal;

/**
 * @author adam
 * @date 29/6/2026 11:58 PM
 */
public class UpdateReceiptRequest {

    private String receiptId;
    private BigDecimal totalAmountPaid;
    private String status;

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public BigDecimal getTotalAmountPaid() { return totalAmountPaid; }
    public void setTotalAmountPaid(BigDecimal totalAmountPaid) { this.totalAmountPaid = totalAmountPaid; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
