package com.alipay.business.common.service.facade.enums;

/**
 * @author adam
 * @date 10/3/2026 1:04 AM
 */
public enum AuthTypeEnum {
    AUTH_TRANSFER("CONFIRM_TRANSFER", "confirm password for transfer operation"),
    AUTH_QUERY_BALANCE("CONFIRM_QUERY_BALANCE", "confirm password for query balance"),
    OTP("OTP", "confirm OTP verification result")

    ;

    private String code;
    private String desc;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    AuthTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}