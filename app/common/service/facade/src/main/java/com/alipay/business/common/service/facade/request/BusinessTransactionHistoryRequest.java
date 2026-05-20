package com.alipay.business.common.service.facade.request;

import com.alipay.account_center.common.service.facade.enums.TransactionCategory;
import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.account_center.common.service.facade.enums.TransactionType;
import com.alipay.business.common.service.facade.baseresult.BusinessBasePageRequest;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class BusinessTransactionHistoryRequest extends BusinessBasePageRequest {
    private String accountId;
    private String payerAccountId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime gmtCreate;
    private int amountMin;
    private int amountMax;
    private String txnStatus;
    private String txnType;
    private String txnCategory;

    public String getAccountId() {
        return this.accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPayerAccountId() {
        return this.payerAccountId;
    }

    public void setPayerAccountId(String payerAccountId) {
        this.payerAccountId = payerAccountId;
    }

    public LocalDateTime getGmtCreate() {
        return this.gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public int getAmountMin() {
        return this.amountMin;
    }

    public void setAmountMin(int amountMin) {
        this.amountMin = amountMin;
    }

    public int getAmountMax() {
        return this.amountMax;
    }

    public void setAmountMax(int amountMax) {
        this.amountMax = amountMax;
    }

    public String getTxnStatus() {
        return this.txnStatus;
    }

    public void setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
    }

    public String getTxnType() {
        return this.txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getTxnCategory() {
        return this.txnCategory;
    }

    public void setTxnCategory(String txnCategory) {
        this.txnCategory = txnCategory;
    }
}
