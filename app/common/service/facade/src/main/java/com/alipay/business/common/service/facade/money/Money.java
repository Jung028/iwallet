package com.alipay.business.common.service.facade.money;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;

public class Money {
    private BigDecimal amount;
    private String currency; // e.g., "USD"


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CurrencyUnit getCurrency() {
        return Monetary.getCurrency(currency);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
