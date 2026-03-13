package com.alipay.business.core.service;

import com.alipay.business.common.dal.auto.dataobject.IdempotencyKeysDO;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeysRepository {
    IdempotencyKeys queryIdempotencyKeysByUniqueRequestId(String uniqueRequestId);

    IdempotencyKeys queryIdempotencyKeysByTxnId(String txnId);

    int updateIdempotencyKeys(String txnId, String status, int retryCount);

    void insertIdempotencyKey(IdempotencyKeys idempotencyKeys);
}
