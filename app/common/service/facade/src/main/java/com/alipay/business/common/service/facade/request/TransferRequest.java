package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.money.Money;

import javax.money.CurrencyUnit;

public class TransferRequest extends BusinessBaseRequest {

    /**
     * payer account number
     */
    private String payerAccountNo;
    /**
     * payee account number
     */
    private String payeeAccountNo;
    /**
     * transfer amount
     */
    private Money amount;
    /**
     * transfer currency
     */
    private CurrencyUnit currency;

    /**
     * unique request id
     */
    private String uniqueRequestId;

    /**
     * get payer account number
     *
     * @return
     */
    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    /**
     * set payer account number
     *
     * @param payerAccountNo
     */
    public void setPayerAccountNo(String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    /**
     * get payee account number
     *
     * @return
     */
    public String getPayeeAccountNo() {
        return payeeAccountNo;
    }

    /**
     * set payee account number
     *
     * @param payeeAccountNo
     */
    public void setPayeeAccountNo(String payeeAccountNo) {
        this.payeeAccountNo = payeeAccountNo;
    }

    /**
     * get transfer amount
     *
     * @return
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * set transfer amount
     *
     * @param amount
     */
    public void setAmount(Money amount) {
        this.amount = amount;
    }

    /**
     * get transfer currency
     * @return
     */
    public CurrencyUnit getCurrency() {
        return currency;
    }

    /**
     * set transfer currency
     * @param currency
     */
    public void setCurrency(CurrencyUnit currency) {
        this.currency = currency;
    }

    /**
     * get unique request id
     * @return
     */
    public String getUniqueRequestId() {
        return uniqueRequestId;
    }

    /**
     * set unique request id
     * @param uniqueRequestId
     */
    public void setUniqueRequestId(String uniqueRequestId) {
        this.uniqueRequestId = uniqueRequestId;
    }
}


