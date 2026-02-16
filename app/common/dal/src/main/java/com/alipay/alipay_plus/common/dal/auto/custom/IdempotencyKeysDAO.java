package com.alipay.alipay_plus.common.dal.auto.custom;

import com.alipay.alipay_plus.common.dal.auto.dataobject.IdempotencyKeysDO;

public interface IdempotencyKeysDAO {
    IdempotencyKeysDO queryIdempotencyKeys(String userId);

    IdempotencyKeysDO insertIdempotencyKey(String uniqueRequestId, String payerAccountNo);
}
