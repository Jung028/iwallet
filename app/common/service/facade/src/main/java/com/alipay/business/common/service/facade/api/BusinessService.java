package com.alipay.business.common.service.facade.api;

import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.common.service.facade.result.BusinessBalanceResult;
import com.alipay.business.common.service.facade.result.BusinessTransactionDetailsResult;
import com.alipay.business.common.service.facade.result.BusinessTransactionHistoryResult;

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
    BusinessBizResult<String> updateIdempotencyKey(UpdateIdempotencyKeysRequest request);
}
