package com.alipay.business.common.service.integration.account;

import com.alipay.alipay_plus.common.service.facade.api.AccountService;
import com.alipay.alipay_plus.common.service.facade.baseresult.AccountBizResult;
import com.alipay.alipay_plus.common.service.facade.item.AccountInfoItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionRecordItem;
import com.alipay.alipay_plus.common.service.facade.request.InsertTransactionRecordRequest;
import com.alipay.alipay_plus.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.alipay_plus.common.service.facade.request.TransferRequest;
import com.alipay.alipay_plus.common.service.facade.request.UpdateTransactionRecordRequest;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.integration.AbstractServiceClient;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.usercenter.common.service.facade.enums.UserResultCode;

public class AccountServiceClientImpl extends AbstractServiceClient implements AccountServiceClient {



    @Override
    public AccountBizResult<String> transfer(TransferRequest request) {
        return null;
    }

    @Override
    public AccountBizResult<AccountInfoItem> queryAccountInfo(QueryAccountInfoRequest request) {
        return null;
    }

    @Override
    public AccountBizResult<String> insertTransactionRecord(InsertTransactionRecordRequest insertTransactionRecordRequest) {
        return null;
    }

    @Override
    public AccountBizResult<TransactionRecordItem> updateTransactionRecord(UpdateTransactionRecordRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL.getCode(), "Update transaction record request cannot be null");
        AssertUtil.notBlank(request.getTxnId(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "txnId cannot be blank");
        AssertUtil.notBlank(request.getStatus(),  BusinessResultCode.PARAM_ILLEGAL.getCode(), "status cannot be blank");

        // set cross invoke
        AccountBizResult<TransactionRecordItem> result = accountService.updateTransactionRecord(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is not success");
        return result;
    }


}
