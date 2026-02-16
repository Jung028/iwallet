package com.alipay.business.biz.service.impl.checker;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.request.QueryTransactionHistoryRequest;
import com.alipay.business.common.service.facade.request.TransferRequest;
import com.alipay.business.core.model.util.AssertUtil;

public class BusinessRequestChecker {

    public static void checkQueryTransactionHistoryRequest(QueryTransactionHistoryRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "request is null");
    }

    public static void checkTransferRequest(TransferRequest request) {
    }
}
