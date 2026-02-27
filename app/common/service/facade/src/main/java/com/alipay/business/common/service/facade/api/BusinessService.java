package com.alipay.business.common.service.facade.api;

import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.item.IdempotencyKeysItem;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.common.service.facade.result.*;

public interface BusinessService {

    /**
     * transfer
     * @param request
     * @return
     */
    BusinessBizResult<String> transfer(TransferRequest request, String userId);

    /**
     * confirm otp for transfer over limit
     * @param request
     * @param userId
     * @return
     */
    BusinessBizResult<String> transferConfirm(TransferConfirmRequest request, String userId);

    /**
     * query transaction details
     * @param request
     * @return
     */
    BusinessBizResult<BusinessTransactionDetailsResult> queryTransactionDetails(BusinessTransactionRecordRequest request);

    /**
     * query transaction history
     * @param request
     * @return
     */
    BusinessBizResult<BusinessTransactionHistoryResult> queryTransactionHistory(BusinessTransactionHistoryRequest request);

    /**
     * query balance
     * @param request
     * @return
     */
    BusinessBizResult<BusinessBalanceResult> queryBalance(BusinessBalanceRequest request);

    /**
     * update idempotency keys
     * @param request
     * @return
     */
    BusinessBizResult<UpdateIdempotencyKeysResult> updateIdempotencyKeys(UpdateIdempotencyKeysRequest request);

    /**
     * query idempotency keys
     * @param request
     * @return
     */
    BusinessBizResult<IdempotencyKeysItem> queryIdempotencyKeys(QueryIdempotencyKeysRequest request);

}
