package com.alipay.business.core.model.exception;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.BusinessResultEnum;

public class BaseSlipException extends RuntimeException {

    public BaseSlipException(BusinessResultCode BusinessResultCode) {
        super(BusinessResultCode.getCode());

    }

    public BaseSlipException(BusinessResultEnum businessResultEnum, String resultMsg) {
        super(businessResultEnum.getResultMsg() + ":" + resultMsg);
    }

    public BaseSlipException(BusinessResultCode businessResultCode, String resultMsg) {
        super(businessResultCode.getDescription() + ":" + resultMsg);
    }

    public BaseSlipException(BusinessResultEnum businessResultEnum) {
        super(businessResultEnum.getResultMsg());
    }
}
