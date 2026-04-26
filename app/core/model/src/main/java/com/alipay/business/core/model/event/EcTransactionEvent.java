package com.alipay.business.core.model.event;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

public class EcTransactionEvent {

    private String transactionId;
    private String payeeAccountId;
    private String payerAccountId;
    private BigDecimal amount;

    public EcTransactionEvent(String transactionId, String payeeAccountId, String payerAccountId, BigDecimal amount) {
        this.transactionId = transactionId;
        this.payeeAccountId = payeeAccountId;
        this.payerAccountId = payerAccountId;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPayeeAccountId() {
        return payeeAccountId;
    }

    public void setPayeeAccountId(String payeeAccountId) {
        this.payeeAccountId = payeeAccountId;
    }

    public String getPayerAccountId() {
        return payerAccountId;
    }

    public void setPayerAccountId(String payerAccountId) {
        this.payerAccountId = payerAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

