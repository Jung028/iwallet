package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.enums.AuthTypeEnum;
import com.alipay.business.common.service.facade.money.Money;
import com.alipay.usercenter.common.service.facade.enums.OTPSceneEnum;

import javax.money.CurrencyUnit;

public class TransferConfirmRequest extends BusinessBaseRequest {
    private String password;
    private String accountId;
    private AuthTypeEnum authTypeEnum;
    private String txnId;

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
}
