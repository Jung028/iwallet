package com.alipay.business.biz.service.impl.business;

import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.common.service.facade.result.QueryBalanceResult;
import com.alipay.business.common.service.facade.result.QueryTransactionDetailsResult;
import com.alipay.business.common.service.facade.result.QueryTransactionHistoryResult;

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
    BusinessBizResult<QueryTransactionDetailsResult> queryTransactionDetails(QueryTransactionDetailsRequest request);

    /**
     * query transaction history
     * @param request
     * @return
     */
    BusinessBizResult<QueryTransactionHistoryResult> queryTransactionHistory(QueryTransactionHistoryRequest request);

    /**
     * query balance
     * @param request
     * @return
     */
    BusinessBizResult<QueryBalanceResult> queryBalance(QueryBalanceRequest request);

}
