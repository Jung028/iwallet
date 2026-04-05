package com.alipay.business.biz.service.impl.checker;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.core.model.util.AssertUtil;
import io.jsonwebtoken.lang.Assert;

public class BusinessRequestChecker {

    public static void checkQueryTransactionHistoryRequest(BusinessTransactionHistoryRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "request is null");
    }

    public static void checkTransferRequest(TransferRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "request is null");
        AssertUtil.notBlank(request.getUniqueRequestId(), BusinessResultCode.PARAM_ILLEGAL, "requestUniqueRequestId is null");
        AssertUtil.notBlank(request.getPayeeAccountNo(), BusinessResultCode.PARAM_ILLEGAL, "requestPayeeAccountNo is null");
        AssertUtil.notBlank(request.getPayerAccountNo(), BusinessResultCode.PARAM_ILLEGAL, "requestPayerAccountNo is null");
        AssertUtil.notNull(request.getAmount(), BusinessResultCode.PARAM_ILLEGAL, "amount is null");
    }

    public static void checkTransferConfirmRequest(TransferConfirmRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "request is null");
        AssertUtil.notBlank(request.getTransferToken(), BusinessResultCode.PARAM_ILLEGAL, "transferToken is required");
        AssertUtil.notBlank(request.getAccountId(), BusinessResultCode.PARAM_ILLEGAL, "requestAccountId is required");
        AssertUtil.notBlank(request.getPassword(), BusinessResultCode.PARAM_ILLEGAL, "requestPassword is required");
    }

    public static void checkQueryTransactionDetailsRequest(BusinessTransactionRecordRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "request is null");
        AssertUtil.notBlank(request.getAccountId(), BusinessResultCode.PARAM_ILLEGAL, "requestAccountId is required");
        AssertUtil.notBlank(request.getTxnId(), BusinessResultCode.PARAM_ILLEGAL, "requestTxnId is required");
    }

    public static void checkQueryBalanceRequest(BusinessBalanceRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "request is null");
        AssertUtil.notBlank(request.getAccountId(), BusinessResultCode.PARAM_ILLEGAL, "requestAccountId is required");
    }

    public static void checkUpdateIdempotencyKeysRequest(UpdateIdempotencyKeysRequest request) {
        // make sure that the request to update the status should only be within
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "request is null");
        AssertUtil.notBlank(request.getReferenceId(), BusinessResultCode.PARAM_ILLEGAL, "reference is required");
        AssertUtil.notBlank(String.valueOf(request.getRetryCount()), BusinessResultCode.PARAM_ILLEGAL, "retry count is required");
        AssertUtil.notBlank(request.getStatus().getCode(), BusinessResultCode.PARAM_ILLEGAL, "status is required");
    }

    public static void checkQueryIdempotencyKeysRequest(QueryIdempotencyKeysRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "request is null");
        AssertUtil.notBlank(request.getReferenceId(), BusinessResultCode.PARAM_ILLEGAL, "requestTxnId is required");
    }

    public static void checkCreateTopUpIntentRequest(TopUpRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "request is null");
        AssertUtil.notBlank(request.getCurrency(),  BusinessResultCode.PARAM_ILLEGAL, "currency is required");
        AssertUtil.notBlank(request.getUserId(),  BusinessResultCode.PARAM_ILLEGAL, "userId required");
        AssertUtil.notBlank(request.getUniqueRequestId(), BusinessResultCode.PARAM_ILLEGAL, "uniqueRequestId is required");
        AssertUtil.notBlank(request.getCardType().toString(), BusinessResultCode.PARAM_ILLEGAL, "card type is required");
        AssertUtil.notBlank(request.getAmount().toString(), BusinessResultCode.PARAM_ILLEGAL, "amount is required");
    }
}
