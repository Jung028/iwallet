package com.alipay.business.biz.service.impl.business;

import com.alipay.account_center.common.service.facade.event.EcTransactionEvent;

/**
 * @author adam
 * @date 15/3/2026 4:36 PM
 */
public interface TransactionService {
    void handleTransactionEvent(EcTransactionEvent event);
}