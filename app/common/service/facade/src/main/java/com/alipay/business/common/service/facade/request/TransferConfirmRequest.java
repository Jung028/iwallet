package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.money.Money;
import com.alipay.usercenter.common.service.facade.enums.OTPSceneEnum;

import javax.money.CurrencyUnit;

public class TransferConfirmRequest extends BusinessBaseRequest {
    private String challengeId;
    private String otp;
    private OTPSceneEnum sceneCode;
    private Money transferAmount;
    private CurrencyUnit transferCurrency;
    private String txnId;

    public String getChallengeId() {
        return this.challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getOtp() {
        return this.otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public OTPSceneEnum getSceneCode() {
        return this.sceneCode;
    }

    public void setSceneCode(OTPSceneEnum sceneCode) {
        this.sceneCode = sceneCode;
    }

    public Money getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Money transferAmount) {
        this.transferAmount = transferAmount;
    }

    public CurrencyUnit getTransferCurrency() {
        return transferCurrency;
    }

    public void setTransferCurrency(CurrencyUnit transferCurrency) {
        this.transferCurrency = transferCurrency;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }
}
