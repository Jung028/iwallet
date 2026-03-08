package com.alipay.business.core.model.converter;


import com.alipay.business.common.dal.auto.dataobject.IdempotencyKeysDO;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import org.springframework.stereotype.Component;

/**
 * @author jung
 * @date 2026-02-14 14:16:16
 */
@Component
public class ModelConverter {

    public IdempotencyKeys convertToModel(IdempotencyKeysDO idempotencyKeysDO) {
        if (idempotencyKeysDO == null) {
            return null;
        }
        IdempotencyKeys keys = new IdempotencyKeys();
        keys.setId(idempotencyKeysDO.getId());
        keys.setUserId(idempotencyKeysDO.getUserId());
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

    public IdempotencyKeysDO convertToDO(IdempotencyKeys idempotencyKeys) {
        if (idempotencyKeys == null) {
            return null;
        }
        IdempotencyKeysDO keys = new IdempotencyKeysDO();
        keys.setId(idempotencyKeys.getId());
        keys.setUserId(idempotencyKeys.getUserId());
        keys.setIdempotencyKey(idempotencyKeys.getIdempotencyKey());
        keys.setRequestHash(idempotencyKeys.getRequestHash());
        keys.setTxnId(idempotencyKeys.getTxnId());
        keys.setStatus(idempotencyKeys.getStatus().getCode());
        keys.setErrorCode(idempotencyKeys.getErrorCode());
        keys.setResponseSnapshot(idempotencyKeys.getResponseSnapshot());
        keys.setCreatedAt(idempotencyKeys.getCreatedAt());
        keys.setUpdatedAt(idempotencyKeys.getUpdatedAt());
        return keys;
    }
}