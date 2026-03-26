package com.alipay.business.core.service.repository;

import com.alipay.business.common.dal.auto.custom.IdempotencyKeysDAO;
import com.alipay.business.common.dal.auto.dataobject.IdempotencyKeysDO;
import com.alipay.business.core.model.converter.ModelConverter;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.exception.RepositoryException;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jung
 * @date 2026-02-14 14:08:00
 */
@Repository
public class IdempotencyKeysRepositoryImpl implements IdempotencyKeysRepository {

    @Autowired
    protected IdempotencyKeysDAO idempotencyKeysDAO;

    @Autowired
    protected ModelConverter modelConverter;

    @Override
    public IdempotencyKeys queryIdempotencyKeysByIdempotencyKey(String idempotencyKey) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.queryIdempotencyKeysByIdempotencyKey(idempotencyKey);
        return modelConverter.convertToModel(idempotencyKeysDO);
    }

    @Override
    public IdempotencyKeys queryIdempotencyKeysByReferenceId(String referenceId) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.queryIdempotencyKeysByReferenceId(referenceId);
        return modelConverter.convertToModel(idempotencyKeysDO);
    }

    @Override
    public IdempotencyKeys queryIdempotencyKeysByRequestHash(String requestHash, Long userId) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.queryIdempotencyKeysByRequestHash(requestHash, userId);
        return modelConverter.convertToModel(idempotencyKeysDO);
    }

    @Override
    public IdempotencyKeys queryActiveIdempotencyKeyByHash(String requestHash, Long userId) {
        IdempotencyKeysDO idempotencyKeysDO = idempotencyKeysDAO.queryActiveIdempotencyKeyByHash(requestHash, userId);
        return modelConverter.convertToModel(idempotencyKeysDO);
    }

    @Override
    public void insertIdempotencyKey(IdempotencyKeys idempotencyKeys) {
        IdempotencyKeysDO idempotencyKeysDO = modelConverter.convertToDO(idempotencyKeys);
        idempotencyKeysDAO.insertIdempotencyKey(idempotencyKeysDO);
    }

    @Override
    public void updateFailedAttempts(IdempotencyKeys idempotencyKeys) {
        try {
            int rows = idempotencyKeysDAO.updateFailedAttempts(idempotencyKeys.getIdempotencyKey(),
                    idempotencyKeys.getStatus().toString(), idempotencyKeys.getRetryCount(),
                    idempotencyKeys.getLockedUntil());
            if (rows <= 0) {
                throw new RepositoryException("Update affected 0 rows for idempotencyKey: "
                        + idempotencyKeys.getIdempotencyKey());
            }
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("DB error during update idempotency key", e);
        }
    }

    @Override
    public void updateReferenceId(String idempotencyKey, String referenceId) {
        try {
            int rows = idempotencyKeysDAO.updateReferenceId(idempotencyKey, referenceId);
            if (rows <= 0) {
                throw new RepositoryException("Update affected 0 rows for idempotencyKey: "
                        + idempotencyKey);
            }
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("DB error during update idempotency key", e);
        }
    }

    @Override
    public int updateIdempotencyKeys(IdempotencyKeys idempotencyKeys) {
        try {
            IdempotencyKeysDO idempotencyKeysDO = modelConverter.convertToDO(idempotencyKeys);
            int rows = idempotencyKeysDAO.updateIdempotencyKeys(idempotencyKeysDO);
            if (rows <= 0) {
                throw new RepositoryException("Update affected 0 rows for idempotencyKey: "
                        + idempotencyKeys.getIdempotencyKey());
            }
            return rows;
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("DB error during update idempotency key", e);
        }
    }

    @Override
    public int countActiveTransactionsByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return idempotencyKeysDAO.countActiveTransactionsByUserId(userId);
    }

    @Override
    public int updateIdempotencyKeysByReferenceId(IdempotencyKeys idempotencyKeys) {
        try {
            IdempotencyKeysDO idempotencyKeysDO = modelConverter.convertToDO(idempotencyKeys);
            int rows = idempotencyKeysDAO.updateIdempotencyKeysByReferenceId(idempotencyKeysDO);
            if (rows <= 0) {
                throw new RepositoryException("Update affected 0 rows for referenceId: " + idempotencyKeys.getReferenceId());
            }
            return rows;
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("DB error during update idempotency key", e);
        }
    }
}
