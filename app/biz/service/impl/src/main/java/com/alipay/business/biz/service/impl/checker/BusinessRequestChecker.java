package com.alipay.business.biz.service.impl.checker;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.core.model.util.AssertUtil;

public class BusinessRequestChecker {

    public static void checkQueryTransactionHistoryRequest(BusinessTransactionHistoryRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL.getCode(), "request is null");
    }

    public static void checkTransferRequest(TransferRequest request) {
    }

    public static void checkTransferConfirmRequest(TransferConfirmRequest request) {

    }

    public static void checkQueryTransactionDetailsRequest(BusinessTransactionRecordRequest request) {
    }

    public static void checkQueryBalanceRequest(BusinessBalanceRequest request) {
    }
}
