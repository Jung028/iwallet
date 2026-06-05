package com.alipay.business.biz.service.impl.helper;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseResult;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.ResultCode;

public class BusinessResultHelper {


    public static <R extends BusinessBaseResult> void fillExceptionResultCode(R result, BusinessResultCode businessResultCode) {
    }

    public static <R extends BusinessBaseResult> void fillSuccessResultCode(R result) {
    }

    public static <R extends BusinessBaseResult> void fillExceptionResultCode(R result, ResultCode resultCode) {
        
    }
}
