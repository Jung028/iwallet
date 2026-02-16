package com.alipay.business.core.service;

import com.alipay.business.core.model.domain.IdempotencyKeys;

public interface IdempotencyKeysRepository {
    IdempotencyKeys queryIdempotencyKeys(String uniqueRequestId);

    void insertIdempotencyKey(String uniqueRequestId, String payerAccountNo);
}
