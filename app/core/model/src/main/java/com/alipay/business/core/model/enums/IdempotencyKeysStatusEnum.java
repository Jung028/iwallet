package com.alipay.business.core.model.enums;


/**
 * @author jung
 * @date 2026-02-14 22:57:49
 */
public enum IdempotencyKeysStatusEnum {
    PENDING("PENDING", "transaction status is pending"),
    SUCCESS("SUCCESS", "transaction is success" ),
    FAILED("FAILED", "failed to insert idempotent record"),

    ;

    private String code;
    private String desc;

    IdempotencyKeysStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

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
}