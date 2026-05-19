package com.alipay.business.common.service.facade.enums;

/**
 * @author adam
 * @date 19/5/2026 5:54 PM
 */
public enum QrType {
    STATIC("STATIC", "Static qr type"),
    DYNAMIC("DYNAMIC", "Dynamic qr type"),
    ;

    QrType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

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
}