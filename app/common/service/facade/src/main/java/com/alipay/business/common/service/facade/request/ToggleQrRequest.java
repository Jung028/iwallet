package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;

/**
 * @author adam
 * @date 19/5/2026 5:32 PM
 */
public class ToggleQrRequest extends BusinessBaseRequest {
    private String qrId;
    private boolean isToggleQr;

    public String getQrId() {
        return qrId;
    }

    public void setQrId(String qrId) {
        this.qrId = qrId;
    }

    public boolean isToggleQr() {
        return isToggleQr;
    }

    public void setToggleQr(boolean toggleQr) {
        isToggleQr = toggleQr;
    }
}