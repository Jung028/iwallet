package com.alipay.business.core.model.enums;

public enum ReceiptStatus {
    UPLOADED("UPLOADED", "receipt is successfully uploaded");

    private String code;
    private String desc;

    ReceiptStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
}
