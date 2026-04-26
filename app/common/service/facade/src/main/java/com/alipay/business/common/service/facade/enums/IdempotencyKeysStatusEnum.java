package com.alipay.business.common.service.facade.enums;

public enum IdempotencyKeysStatusEnum {
    INIT("INIT", "transaction is in INIT state"),
    PENDING("PENDING", "transaction is in pending state"),
    SUCCESS("SUCCESS", "idempotency check is success" ),
    FAILED("FAILED", "failed to insert idempotent record"),
    PROCESSING("PROCESSING", "idempotency check is processing"),
    OTP_OVER_LIMIT("OTP_OVER_LIMIT", "require OTP verification for transfer amount over limit"),
    TIMED_LOCKOUT("TIMED_LOCKOUT", "lockout user from making transfers for a specific time"),
    PERMANENT_LOCKOUT("PERMANENT_LOCKOUT", "lockout user permanently, requires change of password/recovery");
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
