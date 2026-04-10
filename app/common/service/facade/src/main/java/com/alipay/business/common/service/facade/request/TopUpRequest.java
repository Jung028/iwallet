package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.usercenter.common.service.facade.enums.CardType;

import java.math.BigDecimal;

public class TopUpRequest extends BusinessBaseRequest {
    private String userId;
    private BigDecimal amount;
    private String currency;
    private CardType cardType; // CREDIT/DEBIT,
    private String uniqueRequestId;
    private boolean isSaveCard;
    private String passwordPin;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getUniqueRequestId() {
        return uniqueRequestId;
    }

    public void setUniqueRequestId(String uniqueRequestId) {
        this.uniqueRequestId = uniqueRequestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSaveCard() {
        return isSaveCard;
    }

    public void setSaveCard(boolean saveCard) {
        isSaveCard = saveCard;
    }

    public String getPasswordPin() {
        return passwordPin;
    }

    public void setPasswordPin(String passwordPin) {
        this.passwordPin = passwordPin;
    }
}
