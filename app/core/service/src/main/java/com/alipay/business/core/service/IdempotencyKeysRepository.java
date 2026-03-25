package com.alipay.business.core.service;

import com.alipay.business.core.model.domain.IdempotencyKeys;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeysRepository {
    IdempotencyKeys queryIdempotencyKeysByIdempotencyKey(String idempotencyKey);

    IdempotencyKeys queryIdempotencyKeysByTxnId(String txnId);

    IdempotencyKeys queryIdempotencyKeysByRequestHash(String requestHash, Long userId);

    IdempotencyKeys queryActiveIdempotencyKeyByHash(String requestHash, Long userId);

    int updateIdempotencyKeysByTxnId(IdempotencyKeys idempotencyKeys);

    void insertIdempotencyKey(IdempotencyKeys idempotencyKeys);

    void updateFailedAttempts(IdempotencyKeys idempotencyKeys);

    void updateTxnId(String idempotencyKey, String txnId);

    int updateIdempotencyKeys(IdempotencyKeys idempotencyKeys);

    int countActiveTransactionsByUserId(Long userId);

    boolean existsByPaymentIntentId(String id);
}
