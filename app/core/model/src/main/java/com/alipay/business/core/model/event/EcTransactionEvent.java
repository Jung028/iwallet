package com.alipay.business.core.model.event;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

public class EcTransactionEvent {

    private String transactionId;
    private String payeeAccountId;
    private MonetaryAmount amount;
    private String status;

    public EcTransactionEvent(String transactionId, String payeeAccountId, MonetaryAmount amount, String status) {
        this.transactionId = transactionId;
        this.payeeAccountId = payeeAccountId;
        this.amount = amount;
        this.status = status;
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

    public MonetaryAmount getAmount() {
        return amount;
    }

    public void setAmount(MonetaryAmount amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

