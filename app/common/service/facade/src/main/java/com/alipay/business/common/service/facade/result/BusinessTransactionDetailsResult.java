package com.alipay.business.common.service.facade.result;

import com.alipay.alipay_plus.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.alipay_plus.common.service.facade.enums.TransactionTypeEnum;
import com.alipay.business.common.service.facade.baseresult.BusinessBaseResult;

import java.math.BigDecimal;
import java.util.Date;

public class BusinessTransactionDetailsResult extends BusinessBaseResult {
    private String txnId;
    private Date gmtCreate;
    private Date gmtModified;
    private Date gmtComplete;
    private String payerAccountId;
    private String payeeAccountId;
    private BigDecimal amount;
    private String currency;
    private TransactionTypeEnum txnType;
    private TransactionStatusEnum txnStatus;
    private String failureReason;
    private String desc;

    public String getTxnId() {
        return this.txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public Date getGmtCreate() {
        return this.gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return this.gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Date getGmtComplete() {
        return this.gmtComplete;
    }

    public void setGmtComplete(Date gmtComplete) {
        this.gmtComplete = gmtComplete;
    }

    public String getPayerAccountId() {
        return this.payerAccountId;
    }

    public void setPayerAccountId(String payerAccountId) {
        this.payerAccountId = payerAccountId;
    }

    public String getPayeeAccountId() {
        return this.payeeAccountId;
    }

    public void setPayeeAccountId(String payeeAccountId) {
        this.payeeAccountId = payeeAccountId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionTypeEnum getTxnType() {
        return this.txnType;
    }

    public void setTxnType(TransactionTypeEnum txnType) {
        this.txnType = txnType;
    }

    public TransactionStatusEnum getTxnStatus() {
        return this.txnStatus;
    }

    public void setTxnStatus(TransactionStatusEnum txnStatus) {
        this.txnStatus = txnStatus;
    }

    public String getFailureReason() {
        return this.failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
