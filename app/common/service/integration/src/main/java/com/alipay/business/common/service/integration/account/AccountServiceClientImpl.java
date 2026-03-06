package com.alipay.business.common.service.integration.account;

import com.alipay.alipay_plus.common.service.facade.baseresult.AccountBizResult;
import com.alipay.alipay_plus.common.service.facade.item.AccountInfoItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionHistoryItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionRecordItem;
import com.alipay.alipay_plus.common.service.facade.request.*;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.integration.AbstractServiceClient;
import com.alipay.business.core.model.util.AssertUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceClientImpl extends AbstractServiceClient implements AccountServiceClient {


    @Override
    public AccountBizResult<String> createAccount(CreateAccountRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL.getCode(), "Create account request cannot be null");
        AssertUtil.notBlank(request.getOperatorId(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "to account no cannot be blank");

        // set cross invoke
        AccountBizResult<String> result = accountService.createAccount(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is not success");
        return result;
    }

    @Override
    public AccountBizResult<AccountInfoItem> queryAccountInfo(QueryAccountInfoRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL.getCode(), "Query account info request cannot be null");
        AssertUtil.notBlank(request.getAccountId(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "account no cannot be blank");

        // set cross invoke
        AccountBizResult<AccountInfoItem> result = accountService.queryAccountInfo(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is not success");
        return result;
    }

    @Override
    public AccountBizResult<TransactionRecordItem> queryTransactionRecord(QueryTransactionRecordRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL.getCode(), "Query transaction record request cannot be null");
        AssertUtil.notBlank(request.getTxnId(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "transaction Id cannot be blank");
        AssertUtil.notBlank(request.getAccountId(),  BusinessResultCode.PARAM_ILLEGAL.getCode(), "account id cannot be blank");

        // set cross invoke
        AccountBizResult<TransactionRecordItem> result = accountService.queryTransactionRecord(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is not success");
        return result;
    }

    @Override
    public AccountBizResult<List<TransactionHistoryItem>> queryTransactionHistory(QueryTransactionHistoryRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL.getCode(), "Query transaction history request cannot be null");
        AssertUtil.notBlank(request.getTxnId(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "transaction id cannot be blank");
        AssertUtil.notBlank(request.getAccountId(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "account no cannot be blank");

        // set cross invoke
        AccountBizResult <List<TransactionHistoryItem>> result = accountService.queryTransactionHistory(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is not success");
        //convert to business result BusinessTransactionHistoryResult

        return result;
    }

    @Override
    public AccountBizResult<String> insertTransactionRecord(InsertTransactionRecordRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL.getCode(), "Insert transaction record request cannot be null");
        AssertUtil.notBlank(request.getTxnId(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "transaction id cannot be blank");
        AssertUtil.notBlank(request.getPayeeAccountNo(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "payee account no cannot be blank");
        AssertUtil.notBlank(request.getPayerAccountNo(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "payer account no cannot be blank");
        AssertUtil.notNull(request.getAmount(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "amount cannot be blank");
        AssertUtil.notBlank(request.getStatus().getCode(), BusinessResultCode.PARAM_ILLEGAL.getCode(), "status cannot be blank");

        // set cross invoke
        AccountBizResult<String> result = accountService.insertTransactionRecord(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL.getCode(), ", result is not success");
        return result;
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
