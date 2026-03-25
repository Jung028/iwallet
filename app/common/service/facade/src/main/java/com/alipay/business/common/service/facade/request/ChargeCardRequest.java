package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;

import java.math.BigDecimal;

/**
 * @author adam
 * @date 23/3/2026 3:13 PM
 */
public class ChargeCardRequest extends BusinessBaseRequest {
    private BigDecimal amount;
    private String currency;

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