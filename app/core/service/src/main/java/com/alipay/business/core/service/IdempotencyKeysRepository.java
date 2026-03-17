package com.alipay.business.core.service;

import com.alipay.business.common.dal.auto.custom.IdempotencyKeysDAO;
import com.alipay.business.common.dal.auto.dataobject.IdempotencyKeysDO;
import com.alipay.business.common.service.facade.request.UpdateIdempotencyKeysRequest;
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

    int updateIdempotencyKeys(IdempotencyKeys idempotencyKeys);

    int countActiveTransactionsByUserId(Long userId);
}
