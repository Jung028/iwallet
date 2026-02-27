package com.alipay.business.core.model.enums;


public enum BusinessActionEnum {

    TRANSFER("TRANSFER", "transfer amount to anther account"),
    QUERY_TRANSACTION_DETAILS("QUERY_TRANSACTION_DETAILS", "Query transaction details"),
    QUERY_TRANSACTION_HISTORY("QUERY_TRANSACTION_HISTORY", "Query transaction history"),
    QUERY_BALANCE("QUERY_BALANCE", "Query account balance"),
    TRANSFER_CONFIRM_OVER_LIMIT("TRANSFER_CONFIRM_OVER_LIMIT","Transfer confirm over limit"),
    UPDATE_IDEMPOTENCY_KEYS("UPDATE_IDEMPOTENCY_KEYS", "update idempotency keys"),
    QUERY_IDEMPOTENCY_KEYS("QUERY_IDEMPOTENCY_KEYS","query idempotency keys" );

    private String code;
    private String description;

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

    BusinessActionEnum(String code, String description) {

    }
}
