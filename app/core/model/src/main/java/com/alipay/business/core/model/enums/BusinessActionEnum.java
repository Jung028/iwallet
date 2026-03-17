package com.alipay.business.core.model.enums;


public enum BusinessActionEnum {

    TRANSFER("TRANSFER", "transfer amount to anther account"),
    QUERY_TRANSACTION_DETAILS("QUERY_TRANSACTION_DETAILS", "Query transaction details"),
    QUERY_TRANSACTION_HISTORY("QUERY_TRANSACTION_HISTORY", "Query transaction history"),
    QUERY_BALANCE("QUERY_BALANCE", "Query account balance"),
    CONFIRM_TRANSFER("CONFIRM_TRANSFER","Password Confirmation, Initiate transfer"),
    UPDATE_IDEMPOTENCY_KEYS("UPDATE_IDEMPOTENCY_KEYS", "update idempotency keys"),
    QUERY_IDEMPOTENCY_KEYS("QUERY_IDEMPOTENCY_KEYS","query idempotency keys" );

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

    BusinessActionEnum(String code, String description) {

    }
}
