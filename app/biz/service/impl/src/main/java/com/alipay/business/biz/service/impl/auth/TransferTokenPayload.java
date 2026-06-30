package com.alipay.business.biz.service.impl.auth;

import com.alipay.account_center.common.service.facade.enums.TransactionCategory;

import java.math.BigDecimal;

/**
 * @author adam
 * @date 18/3/2026 12:20 AM
 */
public class TransferTokenPayload {
    private String uniqueRequestId;
    private String payerAccountNo;
    private String payeeAccountNo;
    private BigDecimal amount;
    private String currency;       // ISO code e.g. "MYR"
    private boolean requiresOtp;
    private TransactionCategory transactionCategory;
    private String referenceType;
    private String referenceId;

    public String getUniqueRequestId() {
        return uniqueRequestId;
    }

    public void setUniqueRequestId(String uniqueRequestId) {
        this.uniqueRequestId = uniqueRequestId;
    }

    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    public void setPayerAccountNo(String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    public String getPayeeAccountNo() {
        return payeeAccountNo;
    }

    public void setPayeeAccountNo(String payeeAccountNo) {
        this.payeeAccountNo = payeeAccountNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isRequiresOtp() {
        return requiresOtp;
    }

    public void setRequiresOtp(boolean requiresOtp) {
        this.requiresOtp = requiresOtp;
    }

    public TransactionCategory getTransactionCategory() {
        return transactionCategory;
    }

    public void setTransactionCategory(TransactionCategory transactionCategory) {
        this.transactionCategory = transactionCategory;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}