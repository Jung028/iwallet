package com.alipay.business.common.dal.auto.custom;

import com.alipay.business.common.dal.auto.dataobject.IdempotencyKeysDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface IdempotencyKeysDAO {

    IdempotencyKeysDO queryIdempotencyKeysByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    IdempotencyKeysDO queryIdempotencyKeysByReferenceId(@Param("referenceId") String referenceId);

    IdempotencyKeysDO queryIdempotencyKeysByRequestHash(@Param("requestHash") String requestHash, @Param("userId") Long userId);

    IdempotencyKeysDO queryActiveIdempotencyKeyByHash(@Param("requestHash") String requestHash, @Param("userId") Long userId);

    int updateIdempotencyKeysByReferenceId(IdempotencyKeysDO idempotencyKeysDO);

    int updateIdempotencyKeys(IdempotencyKeysDO idempotencyKeysDO);

    void insertIdempotencyKey(IdempotencyKeysDO idempotencyKeysDO);

    int countActiveTransactionsByUserId(@Param("userId") Long userId);

    int updateFailedAttempts(@Param("idempotencyKey") String idempotencyKey, @Param("status") String status,
                             @Param("retryCount") int retryCount,@Param("lockedUntil") Date lockedUntil);

    int updateReferenceId(@Param("idempotencyKey") String idempotencyKey, @Param("referenceId") String referenceId);
}
