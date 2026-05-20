package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBasePageRequest;
import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;

/**
 * @author adam
 * @date 19/5/2026 5:31 PM
 */
public class QueryQrCodesRequest extends BusinessBasePageRequest {
    private String merchantId;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}