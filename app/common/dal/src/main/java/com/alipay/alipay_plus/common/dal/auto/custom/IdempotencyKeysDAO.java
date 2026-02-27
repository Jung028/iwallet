package com.alipay.alipay_plus.common.dal.auto.custom;

import com.alipay.alipay_plus.common.dal.auto.dataobject.IdempotencyKeysDO;

public interface IdempotencyKeysDAO {

    IdempotencyKeysDO queryIdempotencyKeysByUniqueRequestId(String uniqueRequestId);

    IdempotencyKeysDO queryIdempotencyKeysByTxnId(String txnId);
    // SQL should check if PENDING only can update
    IdempotencyKeysDO updateIdempotencyKeys(String txnId, String status, int retryCount);

    IdempotencyKeysDO insertIdempotencyKey(String uniqueRequestId, String payerAccountNo);


}
