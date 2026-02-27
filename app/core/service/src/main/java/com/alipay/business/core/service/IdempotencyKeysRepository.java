package com.alipay.business.core.service;

import com.alipay.business.core.model.domain.IdempotencyKeys;

public interface IdempotencyKeysRepository {
    IdempotencyKeys queryIdempotencyKeysByUniqueRequestId(String uniqueRequestId);

    IdempotencyKeys queryIdempotencyKeysByTxnId(String txnId);

    IdempotencyKeys updateIdempotencyKeys(String txnId, String status, int retryCount);

    void insertIdempotencyKey(String uniqueRequestId, String payerAccountNo);
}
