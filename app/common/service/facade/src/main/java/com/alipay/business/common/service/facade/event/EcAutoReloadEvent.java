package com.alipay.business.common.service.facade.event;

import java.math.BigDecimal;

/**
 * @author adam
 * @date 26/3/2026 6:44 PM
 */
public class EcAutoReloadEvent {
    private String userId;
    private BigDecimal amount;
    private String currency;

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
}