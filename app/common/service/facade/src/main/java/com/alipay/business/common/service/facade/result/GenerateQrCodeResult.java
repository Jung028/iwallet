package com.alipay.business.common.service.facade.result;

/**
 * @author adam
 * @date 24/4/2026 7:48 PM
 */
public class GenerateQrCodeResult {
    private String qrId;
    private String payload;
    private String signature;

    public GenerateQrCodeResult(String qrId, String payload, String signature) {
        this.qrId = qrId;
        this.payload = payload;
        this.signature = signature;
    }

    public String getQrId() {
        return qrId;
    }

    public void setQrId(String qrId) {
        this.qrId = qrId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}