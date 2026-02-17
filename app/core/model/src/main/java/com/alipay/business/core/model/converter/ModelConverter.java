package com.alipay.business.core.model.converter;


import com.alipay.alipay_plus.common.dal.auto.dataobject.IdempotencyKeysDO;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.enums.IdempotencyKeysStatusEnum;

/**
 * @author jung
 * @date 2026-02-14 14:16:16
 */
public class ModelConverter {

    public IdempotencyKeys convertToModel(IdempotencyKeysDO idempotencyKeysDO) {
        if (idempotencyKeysDO == null) {
            return null;
        }
        IdempotencyKeys keys = new IdempotencyKeys();
        keys.setId(idempotencyKeysDO.getId());
        keys.setIdempotencyKey(idempotencyKeysDO.getIdempotencyKey());
        keys.setRequestHash(idempotencyKeysDO.getRequestHash());
        keys.setTxnId(idempotencyKeysDO.getTxnId());
        keys.setStatus(IdempotencyKeysStatusEnum.valueOf(idempotencyKeysDO.getStatus()));
        keys.setErrorCode(idempotencyKeysDO.getErrorCode());
        keys.setResponseSnapshot(idempotencyKeysDO.getResponseSnapshot());
        keys.setCreatedAt(idempotencyKeysDO.getCreatedAt());
        keys.setUpdatedAt(idempotencyKeysDO.getUpdatedAt());
        return keys;
    }
}