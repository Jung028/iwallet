package com.alipay.business.common.service.facade.result;

public class ConfirmUploadResponse {

    private String sessionId;
    private String qrToken;

    public ConfirmUploadResponse() {}

    public ConfirmUploadResponse(String sessionId, String qrToken) {
        this.sessionId = sessionId;
        this.qrToken = qrToken;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }
}
