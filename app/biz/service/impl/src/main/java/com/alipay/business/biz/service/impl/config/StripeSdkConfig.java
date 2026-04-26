package com.alipay.business.biz.service.impl.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeSdkConfig {

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException(
                    "Missing Stripe secret key. Set 'stripe.secret-key' or env var STRIPE_SECRET_KEY.");
        }
        Stripe.apiKey = stripeSecretKey.trim();
    }
}
