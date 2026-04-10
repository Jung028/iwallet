package com.alipay.business.common.service.facade.result;

/**
 * @author adam
 * @date 24/3/2026 9:03 PM
 */
public class TopUpResult {
    private String secretClient;
    private String paymentIntentId;
    private String txnId;

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

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }
}