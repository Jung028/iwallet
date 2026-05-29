package com.alipay.business.core.model.exception;

import com.alipay.business.common.service.facade.enums.BusinessResultEnum;
import com.alipay.business.common.service.facade.enums.ResultCode;

public class BaseSlipException extends RuntimeException {

    public BaseSlipException(ResultCode resultCode) {
        super(resultCode.getCode());
    }

    public BaseSlipException(ResultCode resultCode, String resultMsg) {
        super(resultCode.getDescription() + ":" + resultMsg);
    }

    public BaseSlipException(BusinessResultEnum businessResultEnum, String resultMsg) {
        super(businessResultEnum.getResultMsg() + ":" + resultMsg);
    }

    public BaseSlipException(BusinessResultEnum businessResultEnum) {
        super(businessResultEnum.getResultMsg());
    }
}
