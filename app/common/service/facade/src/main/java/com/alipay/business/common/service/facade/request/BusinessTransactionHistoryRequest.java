package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;

public class BusinessTransactionHistoryRequest extends BusinessBaseRequest {
    private String accountId;
    private String txnId;

    public String getAccountId() {
        return this.accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTxnId() {
        return this.txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }
}
