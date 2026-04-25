package com.alipay.business.common.service.facade.result;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseResult;

public class UpdateIdempotencyKeysResult {
    private String status;
    private String referenceId;
    private int retryCount;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
