package com.alipay.business.common.service.facade.enums;

public enum BusinessResultEnum implements IpayCommonResultCode{
    EXECUTE_SUCCESS("0200", ResultCodeLevel.INFO, ResultCodeType.SUCCESS, "Execution successful"),
    SYSTEM_EXCEPTION("0500", ResultCodeLevel.ERROR, ResultCodeType.SYS_ERROR, "System exception occurred"),
    PARAM_ILLEGAL("0400", ResultCodeLevel.WARN, ResultCodeType.BIZ_ERROR, "Illegal parameter");


    public String code;
    public String description;
    private String errorType;
    private String errorLevel;

    BusinessResultEnum(String code, String errorLevel, String errorType, String description) {
        this.code = code;
        this.errorLevel = errorLevel;
        this.errorType = errorType;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(String errorLevel) {
        this.errorLevel = errorLevel;
    }

    @Override
    public String getResultCode() {
        return getCode();
    }

    @Override
    public String getResultMsg() {
        return getDescription();
    }
}
