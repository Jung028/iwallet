package com.alipay.business.core.service.repository;

import com.alipay.alipay_plus.common.dal.auto.custom.IdempotencyKeysDAO;
import com.alipay.alipay_plus.common.dal.auto.dataobject.IdempotencyKeysDO;
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
    public IdempotencyKeys queryIdempotencyKeys(String userId) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.queryIdempotencyKeys(userId);
        return modelConverter.convertToModel(idempotencyKeysDO);
    }

    @Override
    public IdempotencyKeys updateIdempotencyKeys(String uniqueRequestId, String status) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.updateIdempotencyKeys(uniqueRequestId, status);
        return modelConverter.convertToModel(idempotencyKeysDO);
    }

    @Override
    public void insertIdempotencyKey(String uniqueRequestId, String payerAccountNo) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.insertIdempotencyKey(uniqueRequestId, payerAccountNo);
        modelConverter.convertToModel(idempotencyKeysDO);
    }
}
