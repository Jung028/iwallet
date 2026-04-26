package com.alipay.business.biz.service.impl.business;

/**
 * @author adam
 * @date 15/3/2026 4:36 PM
 */
public interface TransactionService {
    void publishTransfer(String payerAccountNo, String txnId, String txnEventType);
}