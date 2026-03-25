package com.alipay.business.common.service.facade.result;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseResult;

/**
 * @author adam
 * @date 24/3/2026 9:03 PM
 */
public class TopUpResult extends BusinessBaseResult {
    private String secretClient;
    private String paymentIntentId;

    public String getSecretClient() {
        return secretClient;
    }

    public void setSecretClient(String secretClient) {
        this.secretClient = secretClient;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
}