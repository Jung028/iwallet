package com.alipay.business.common.dal.auto.custom;

import com.alipay.business.common.dal.auto.dataobject.IdempotencyKeysDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface IdempotencyKeysDAO {

    IdempotencyKeysDO queryIdempotencyKeysByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    IdempotencyKeysDO queryIdempotencyKeysByTxnId(@Param("txnId") String txnId);

    IdempotencyKeysDO queryIdempotencyKeysByRequestHash(@Param("requestHash") String requestHash, @Param("userId") Long userId);

    IdempotencyKeysDO queryActiveIdempotencyKeyByHash(@Param("requestHash") String requestHash, @Param("userId") Long userId);

    int updateIdempotencyKeysByTxnId(IdempotencyKeysDO idempotencyKeysDO);

    int updateIdempotencyKeys(IdempotencyKeysDO idempotencyKeysDO);

    int insertIdempotencyKey(IdempotencyKeysDO idempotencyKeysDO);

    int countActiveTransactionsByUserId(@Param("userId") Long userId);

    int updateFailedAttempts(@Param("idempotencyKey") String idempotencyKey, @Param("status") String status,
                             @Param("retryCount") int retryCount,@Param("lockedUntil") Date lockedUntil);

    int updateTxnId(@Param("idempotencyKey") String idempotencyKey, @Param("txnId") String txnId);
}
