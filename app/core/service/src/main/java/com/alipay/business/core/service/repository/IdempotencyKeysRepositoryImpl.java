package com.alipay.business.core.service.repository;

import com.alipay.business.common.dal.auto.custom.IdempotencyKeysDAO;
import com.alipay.business.common.dal.auto.dataobject.IdempotencyKeysDO;
import com.alipay.business.core.model.converter.ModelConverter;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.service.IdempotencyKeysRepository;

/**
 * @author jung
 * @date 2026-02-14 14:08:00
 */
public class IdempotencyKeysRepositoryImpl implements IdempotencyKeysRepository {

    protected IdempotencyKeysDAO idempotencyKeysDAO;

    protected ModelConverter modelConverter;

    @Override
    public IdempotencyKeys queryIdempotencyKeysByUniqueRequestId(String uniqueRequestId) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.queryIdempotencyKeysByUniqueRequestId(uniqueRequestId);
        return modelConverter.convertToModel(idempotencyKeysDO);
    }

    @Override
    public IdempotencyKeys queryIdempotencyKeysByTxnId(String txnId) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.queryIdempotencyKeysByTxnId(txnId);
        return modelConverter.convertToModel(idempotencyKeysDO);
    }

    @Override
    public IdempotencyKeys updateIdempotencyKeys(String txnId, String status, int retryCount) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.updateIdempotencyKeys(txnId, status, retryCount);
        return modelConverter.convertToModel(idempotencyKeysDO);
    }

    @Override
    public void insertIdempotencyKey(String uniqueRequestId, String payerAccountNo) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.insertIdempotencyKey(uniqueRequestId, payerAccountNo);
        modelConverter.convertToModel(idempotencyKeysDO);
    }
}
