package com.alipay.business.common.util.enums;

public enum IpayTenantEnum {
    IPAY_SG, IPAY_US;
    private String tntInstId;
    private String timeZone;

    public String getTntInstId() {
        return tntInstId;
    }

    public void setTntInstId(String tntInstId) {
        this.tntInstId = tntInstId;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
