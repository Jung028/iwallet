package com.alipay.business.common.service.facade.result;

public class CommitSessionResponse {
    private String qrToken;

    public CommitSessionResponse(String qrToken) { this.qrToken = qrToken; }

    public String getQrToken() { return qrToken; }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}
