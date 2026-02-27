package com.alipay.business.common.service.facade.item;

import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;

public class IdempotencyKeysItem {
    private String txnId;
    private String status;
    private Long userId;
    private int retryCount;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
