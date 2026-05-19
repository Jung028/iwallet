package com.alipay.business.common.service.facade.item;

import java.math.BigDecimal;
import java.util.Date;

public class QrCodeItem {
    private String qrId;
    private String ownerId;
    private String ownerType;
    private String intent;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Date expiresAt;
    private Date createdAt;

    public String getQrId() { return qrId; }
    public void setQrId(String qrId) { this.qrId = qrId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerType() { return ownerType; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
