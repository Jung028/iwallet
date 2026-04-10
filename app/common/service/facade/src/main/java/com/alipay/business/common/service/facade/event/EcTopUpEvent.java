package com.alipay.business.common.service.facade.event;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author adam
 * @date 24/3/2026 10:07 PM
 */
public class EcTopUpEvent {
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String paymentIntentId;
    private String payerAccountId;
    private String payeeAccountId;
    private Date gmtTaskOccur;

    public EcTopUpEvent(String userId, BigDecimal amount, String currency, String paymentIntentId, String payerAccountId, String payeeAccountId, Date gmtTaskOccur) {
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.paymentIntentId = paymentIntentId;
        this.payerAccountId = payerAccountId;
        this.payeeAccountId = payeeAccountId;
        this.gmtTaskOccur = gmtTaskOccur;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getPayerAccountId() {
        return payerAccountId;
    }

    public void setPayerAccountId(String payerAccountId) {
        this.payerAccountId = payerAccountId;
    }

    public String getPayeeAccountId() {
        return payeeAccountId;
    }

    public void setPayeeAccountId(String payeeAccountId) {
        this.payeeAccountId = payeeAccountId;
    }

    public Date getGmtTaskOccur() {
        return gmtTaskOccur;
    }

    public void setGmtTaskOccur(Date gmtTaskOccur) {
        this.gmtTaskOccur = gmtTaskOccur;
    }
}