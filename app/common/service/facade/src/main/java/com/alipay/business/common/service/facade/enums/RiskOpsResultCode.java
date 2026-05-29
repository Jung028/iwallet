package com.alipay.business.common.service.facade.enums;

import static com.alipay.business.common.service.facade.enums.ErrorCodeConstants.PREFIX;

public enum RiskOpsResultCode implements ResultCode {

    EXECUTE_SUCCESS(GlobalResultCodes.EXECUTE_SUCCESS, "Success"),

    SYSTEM_EXCEPTION(GlobalResultCodes.SYSTEM_EXCEPTION, "System Exception"),

    PARAM_ILLEGAL(GlobalResultCodes.PARAM_ILLEGAL, "Parameter Illegal"),

    RISK_DECISION_NOT_FOUND(ResultCodeLevel.WARN, ResultCodeType.BIZ_ERROR, RiskOpsBizType.RISK_OPS, "01", "Risk Decision Not Found"),
    RISK_EVALUATION_FAILED(ResultCodeLevel.ERROR, ResultCodeType.BIZ_ERROR, RiskOpsBizType.RISK_OPS, "02", "Risk Evaluation Failed"),
    RISK_SIGNAL_INVALID(ResultCodeLevel.WARN, ResultCodeType.BIZ_ERROR, RiskOpsBizType.RISK_OPS, "03", "Risk Signal Invalid"),

    ;

    private final String code;

    private final String description;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    RiskOpsResultCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    RiskOpsResultCode(String level, String bizError, String bizType, String errorSpecific, String description) {
        this.code = PREFIX + level + bizError + SystemCode.I_IDIGITALRISK + bizType + errorSpecific;
        this.description = description;
    }
}
