package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBasePageRequest;
import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;

/**
 * @author adam
 * @date 30/6/2026 5:36 PM
 */
public class QueryReceiptItemsRequest extends BusinessBasePageRequest {
    private String receiptId;
    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

}