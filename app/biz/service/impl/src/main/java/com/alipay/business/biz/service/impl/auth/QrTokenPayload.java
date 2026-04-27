package com.alipay.business.biz.service.impl.auth;

import java.util.Date;

/**
 * @author adam
 * @date 26/4/2026 3:46 PM
 */
public class QrTokenPayload {
    private String qrId;
    private Long amount;
    private String currency;
    private String ownerId;
    private String qrIntent;
    private Date expiresAt;

    public String getQrId() {
        return qrId;
    }

    public void setQrId(String qrId) {
        this.qrId = qrId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getQrIntent() {
        return qrIntent;
    }

    public void setQrIntent(String qrIntent) {
        this.qrIntent = qrIntent;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}