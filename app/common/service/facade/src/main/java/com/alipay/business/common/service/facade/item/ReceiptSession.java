package com.alipay.business.common.service.facade.item;

import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.business.common.service.facade.enums.ReceiptSessionStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author adam
 * @date 28/6/2026 6:02 PM
 */
public class ReceiptSession {

    private String receiptId;

    private BigDecimal totalPaid;

    private BigDecimal totalUnpaid;

    private ReceiptSessionStatus status;

    private Date gmtCreate;

    private String receiptName;

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalUnpaid() {
        return totalUnpaid;
    }

    public void setTotalUnpaid(BigDecimal totalUnpaid) {
        this.totalUnpaid = totalUnpaid;
    }

    public ReceiptSessionStatus getStatus() {
        return status;
    }

    public void setStatus(ReceiptSessionStatus status) {
        this.status = status;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getReceiptName() {
        return receiptName;
    }

    public void setReceiptName(String receiptName) {
        this.receiptName = receiptName;
    }

}
