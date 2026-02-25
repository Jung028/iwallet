package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;

public class UpdateIdempotencyKeysRequest extends BusinessBaseRequest {
    private String uniqueRequestId;
    private IdempotencyKeysStatusEnum status;

    public String getUniqueRequestId() {
        return uniqueRequestId;
    }

    public void setUniqueRequestId(String uniqueRequestId) {
        this.uniqueRequestId = uniqueRequestId;
    }

    public IdempotencyKeysStatusEnum getStatus() {
        return status;
    }

    public void setStatus(IdempotencyKeysStatusEnum status) {
        this.status = status;
    }
}
