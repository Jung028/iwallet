package com.alipay.business.common.service.facade.enums;

/**
 * @author adam
 * @date 5/4/2026 11:28 AM
 */
public enum IdempotencyTypeEnum {
    TOP_UP("TOP_UP", "Top up"),
    TRANSFER("TRANSFER", "transfer"),
    TRANSFER_INCORRECT_PIN("TRANSFER_INCORRECT_PIN", "transfer incorrect pin"),

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

    IdempotencyTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}