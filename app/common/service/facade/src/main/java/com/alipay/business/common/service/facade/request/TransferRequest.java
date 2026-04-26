package com.alipay.business.common.service.facade.request;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseRequest;
import com.alipay.business.common.service.facade.money.Money;

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
     * unique request id
     */
    private String uniqueRequestId;

    /**
     * transfer type, need to know if its QR.
     */
    private String transferType;

    /**
     * qr token
     */
    private String qrToken;

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

    /**
     * get transfer type
     * @return
     */
    public String getTransferType() {
        return transferType;
    }

    /**
     * set transfer type
     * @param transferType
     */
    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    /**
     * get qr token
     * @return
     */
    public String getQrToken() {
        return qrToken;
    }

    /**
     * set qr token
     * @param qrToken
     */
    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}


