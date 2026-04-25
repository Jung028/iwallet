package com.alipay.business.common.service.facade.enums;

/**
 * @author adam
 * @date 24/4/2026 12:47 AM
 */
public enum QrCodeStatus {
    INIT("INIT", "Qr Code intialized"),
    ACTIVE("ACTIVE", "Qr code is active"),
    ;
    private String code;
    private String desc;

    QrCodeStatus(String code, String desc) {
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
