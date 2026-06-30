package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBasePageRequest;

/**
 * @author adam
 * @date 28/6/2026 4:11 PM
 */
public class QueryReceiptsHistoryRequest extends BusinessBasePageRequest {
    private String receiptId;
    private String userId;

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}