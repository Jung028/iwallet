package com.alipay.business.common.service.facade.money;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import org.javamoney.moneta.Money;

import java.math.BigDecimal;


import javax.money.RoundingQueryBuilder;
import java.math.RoundingMode;

/**
 * Production-ready Money / FX utility
 */
public class MoneyUtil {

    public static MonetaryAmount toMonetaryAmount(BigDecimal amount, CurrencyUnit currency) {
        return Money.of(amount, currency);
    }

    public static BigDecimal toDecimal(MonetaryAmount amount) {
        return amount.getNumber().numberValue(BigDecimal.class);
    }

    public static String getCurrency(MonetaryAmount amount) {
        return amount.getCurrency().getCurrencyCode();
    }

    /**
     * Safe addition
     */
    public static MonetaryAmount add(MonetaryAmount a, MonetaryAmount b) {
        return a.add(b);
    }

    /**
     * Safe subtraction
     */
    public static MonetaryAmount subtract(MonetaryAmount a, MonetaryAmount b) {
        return a.subtract(b);
    }

    /**
     * FX conversion
     *
     * @param source        source amount
     * @param fxRate        explicit FX rate
     * @param targetCurrency target currency code
     * @return converted amount
     */
    public static MonetaryAmount convertFx(MonetaryAmount source, BigDecimal fxRate, String targetCurrency) {
        MonetaryAmount multiplied = source.multiply(fxRate);
        // Banker's rounding using ISO currency
        MonetaryAmount rounded = multiplied.with(
                Monetary.getRounding(
                        RoundingQueryBuilder.of()
                                .setCurrency(Monetary.getCurrency(targetCurrency))
                                .set(RoundingMode.HALF_EVEN)
                                .build()
                )
        );
        return Money.of(rounded.getNumber().numberValue(BigDecimal.class), targetCurrency);
    }

    /**
     * Convert MonetaryAmount -> minor units (for ledger / DB)
     */
    public static long toMinorUnits(MonetaryAmount amount) {
        int scale = amount.getCurrency().getDefaultFractionDigits();
        BigDecimal bd = amount.getNumber().numberValue(BigDecimal.class);
        return bd.movePointRight(scale).longValueExact();
    }

    /**
     * Convert minor units from ledger -> MonetaryAmount
     */
    public static MonetaryAmount fromMinorUnits(long minor, String currency) {
        int scale = Monetary.getCurrency(currency).getDefaultFractionDigits();
        BigDecimal bd = BigDecimal.valueOf(minor, scale);
        return Money.of(bd, currency);
    }
}