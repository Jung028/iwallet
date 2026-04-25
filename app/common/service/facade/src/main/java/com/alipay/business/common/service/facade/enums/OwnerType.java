package com.alipay.business.common.service.facade.enums;

/**
 * @author adam
 * @date 25/4/2026 2:15 PM
 */
public enum OwnerType {
    USER("USER","user generated the QR"),
    MERCHANT("MERCHANT", "merchant generated the QR");

    private String code;
    private String desc;

    OwnerType(String code, String desc) {
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