package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.enums.AuthTypeEnum;

public class TransferConfirmRequest extends BusinessBaseRequest {
    private String password;
    private String accountId;
    private AuthTypeEnum authTypeEnum;
    private String txnId;
    private String verifiedToken;
    private String transferToken;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public AuthTypeEnum getAuthTypeEnum() {
        return authTypeEnum;
    }

    public void setAuthTypeEnum(AuthTypeEnum authTypeEnum) {
        this.authTypeEnum = authTypeEnum;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getVerifiedToken() {
        return verifiedToken;
    }

    public void setVerifiedToken(String verifiedToken) {
        this.verifiedToken = verifiedToken;
    }

    public String getTransferToken() {
        return transferToken;
    }

    public void setTransferToken(String transferToken) {
        this.transferToken = transferToken;
    }
}
