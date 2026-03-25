package com.alipay.business.core.service;

import com.alipay.business.core.model.domain.IdempotencyKeys;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeysRepository {
    IdempotencyKeys queryIdempotencyKeysByIdempotencyKey(String idempotencyKey);

    IdempotencyKeys queryIdempotencyKeysByReferenceId(String referenceId);

    IdempotencyKeys queryIdempotencyKeysByRequestHash(String requestHash, Long userId);

    IdempotencyKeys queryActiveIdempotencyKeyByHash(String requestHash, Long userId);

    int updateIdempotencyKeysByReferenceId(IdempotencyKeys idempotencyKeys);

    void insertIdempotencyKey(IdempotencyKeys idempotencyKeys);

    void updateFailedAttempts(IdempotencyKeys idempotencyKeys);

    void updateReferenceId(String idempotencyKey, String referenceId);

    int updateIdempotencyKeys(IdempotencyKeys idempotencyKeys);

    int countActiveTransactionsByUserId(Long userId);

    boolean existsByPaymentIntentId(String id);
}
