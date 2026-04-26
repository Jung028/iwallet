package com.alipay.business.common.service.facade.enums;

/**
 * @author adam
 * @date 23/4/2026 11:16 PM
 */
public enum QrIntent {
    P2P("P2P", "Payer to Payee, or Person to Person"),
    P2M("P2M", "Payer to Merchant"),
    R2P("R2P", "Request to Pay")
    ;

    private String code;
    private String desc;

    QrIntent(String code, String desc) {
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