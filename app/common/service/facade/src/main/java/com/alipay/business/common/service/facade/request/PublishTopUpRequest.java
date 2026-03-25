package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;

/**
 * @author adam
 * @date 23/3/2026 3:13 PM
 */
public class PublishTopUpRequest extends BusinessBaseRequest {
    private String payload;
    private String stripeSignature;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getStripeSignature() {
        return stripeSignature;
    }

    public void setStripeSignature(String stripeSignature) {
        this.stripeSignature = stripeSignature;
    }
}