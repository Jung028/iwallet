package com.alipay.business.common.service.facade.enums;

public enum IdempotencyKeysStatusEnum {
    DEBIT_CREDIT_UPDATE_FAILED("DEBIT_CREDIT_UPDATE_FAILED", "Either the debit or credit failed"),
    INIT("INIT", "transaction is in INIT state"),
    PENDING("PENDING", "transaction is in pending state"),
    SUCCESS("SUCCESS", "idempotency check is success" ),
    FAILED("FAILED", "failed to insert idempotent record"),
    PROCESSING("PROCESSING", "idempotency check is processing")

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
