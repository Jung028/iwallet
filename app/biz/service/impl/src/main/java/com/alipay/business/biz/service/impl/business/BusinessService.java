package com.alipay.business.biz.service.impl.business;

import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.request.QueryBalanceRequest;
import com.alipay.business.common.service.facade.request.QueryTransactionDetailsRequest;
import com.alipay.business.common.service.facade.request.QueryTransactionHistoryRequest;
import com.alipay.business.common.service.facade.request.TransferRequest;
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
