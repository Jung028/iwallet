package com.alipay.business.common.service.facade.enums;

/**
 * @author adam
 * @date 6/4/2026 11:51 AM
 */
public enum TransferType {
    OTP("OTP", "requires OTP"),
    AUTH_TRANSFER("AUTH_TRANSFER", "requires auth");

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

    TransferType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}