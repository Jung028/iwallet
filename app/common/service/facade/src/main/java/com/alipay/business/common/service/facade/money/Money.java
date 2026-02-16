package com.alipay.business.common.service.facade.money;

import javax.money.CurrencyUnit;
import java.math.BigDecimal;

public class Money {
    private BigDecimal amount;
    private CurrencyUnit currency;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CurrencyUnit getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyUnit currency) {
        this.currency = currency;
    }
}
