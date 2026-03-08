package com.alipay.business.common.dal.auto.custom;

import com.alipay.business.common.dal.auto.dataobject.IdempotencyKeysDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IdempotencyKeysDAO {

    IdempotencyKeysDO queryIdempotencyKeysByUniqueRequestId(@Param("uniqueRequestId") String uniqueRequestId);

    IdempotencyKeysDO queryIdempotencyKeysByTxnId(@Param("txnId") String txnId);

    IdempotencyKeysDO updateIdempotencyKeys(@Param("txnId") String txnId,
                                            @Param("status") String status,
                                            @Param("retryCount") int retryCount);

    void insertIdempotencyKey(IdempotencyKeysDO idempotencyKeysDO);

}
