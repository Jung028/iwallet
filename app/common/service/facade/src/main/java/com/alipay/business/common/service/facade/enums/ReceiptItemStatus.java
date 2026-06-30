package com.alipay.business.common.service.facade.enums;

/**
 * @author adam
 * @date 28/6/2026 4:13 PM
 */
public enum ReceiptItemStatus {
    UNPAID("UNPAID", "receipt item is not paid yet"),
    PAID("PAID", "receipt item has been paid for"),
    SETTLED("SETTLED", "receipt item has been settled offline by admin user"); // for admin update
    private String code;
    private String desc;

    ReceiptItemStatus(String settled, String s) {
        this.code = settled;
        this.desc = s;
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