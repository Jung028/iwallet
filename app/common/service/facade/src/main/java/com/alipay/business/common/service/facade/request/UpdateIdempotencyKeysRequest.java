package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;

public class UpdateIdempotencyKeysRequest extends BusinessBaseRequest {
    private String referenceId;
    private IdempotencyKeysStatusEnum status;
    private int retryCount;

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public IdempotencyKeysStatusEnum getStatus() {
        return status;
    }

    public void setStatus(IdempotencyKeysStatusEnum status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
