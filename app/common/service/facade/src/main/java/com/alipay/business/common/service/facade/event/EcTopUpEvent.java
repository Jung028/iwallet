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
    private long gmtTaskOccur;

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

    public long getGmtTaskOccur() {
        return gmtTaskOccur;
    }

    public void setGmtTaskOccur(long gmtTaskOccur) {
        this.gmtTaskOccur = gmtTaskOccur;
    }
}