package com.alipay.business.common.service.facade.enums;


import static com.alipay.business.common.service.facade.enums.ErrorCodeConstants.PREFIX;

public enum BusinessResultCode {


    EXECUTE_SUCCESS(GlobalResultCodes.EXECUTE_SUCCESS, "Success"),


    SYSTEM_EXCEPTION(GlobalResultCodes.SYSTEM_EXCEPTION, "System Exception"),


    PARAM_ILLEGAL(GlobalResultCodes.PARAM_ILLEGAL, "Parameter Illegal"),


    REPEATED_SUBMIT(GlobalResultCodes.REPEATED_SUBMIT, "Repeated Submit"),


    RISK_SCENE_NOT_EXIST(ResultCodeLevel.WARN, ResultCodeType.BIZ_ERROR, BusinessBizType.RISK_SCENE, "01", "Risk Scene Not Exist"),
    ACCOUNT_NOT_FOUND(ResultCodeLevel.WARN, ResultCodeType.BIZ_ERROR, BusinessBizType.COMMON, "02", "Account Not Found"),
    INVALID_REQUEST(ResultCodeLevel.ERROR, ResultCodeType.BIZ_ERROR, BusinessBizType.COMMON, "03", "Invalid Request"),
    PASSWORD_INCORRECT(ResultCodeLevel.WARN, ResultCodeType.BIZ_ERROR, BusinessBizType.COMMON, "04", "Password Incorrect" ),
    ILLEGAL_STATUS(ResultCodeLevel.ERROR, ResultCodeType.BIZ_ERROR, BusinessBizType.COMMON, "05", "Transaction Status is illegal");

    private final String code;

    private final String description;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }

    BusinessResultCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    BusinessResultCode(String level, String bizError, String bizType, String errorSpecific, String description) {
        this.code = PREFIX + level + bizError + SystemCode.I_SLIPCORE + bizType + errorSpecific;
        this.description = description;
    }
}
