package com.alipay.business.core.model.exception;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.SlipResultEnum;

public class BaseSlipException extends RuntimeException {

    public BaseSlipException(SlipResultEnum slipResultEnum) {
        super(slipResultEnum.getResultMsg());

    }

    public BaseSlipException(SlipResultEnum slipResultEnum, String resultMsg) {
        super(slipResultEnum.getResultMsg() + ":" + resultMsg);
    }

    public BaseSlipException(BusinessResultCode businessResultCode, String resultMsg) {
        super(businessResultCode.getDescription() + ":" + resultMsg);
    }
}
