package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;

/**
 * @author adam
 * @date 12/7/2026 8:29 PM
 */
public class QueryTransactionReceiptItemRelRequest extends BusinessBaseRequest {
    private String txnId;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }
}