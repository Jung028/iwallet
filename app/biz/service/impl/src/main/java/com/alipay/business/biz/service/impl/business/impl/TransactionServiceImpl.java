package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.account_center.common.service.facade.event.EcTransactionEvent;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import org.springframework.stereotype.Service;

/**
 * @author adam
 * @date 15/3/2026 4:37 PM
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    @Override
    public void handleTransactionEvent(EcTransactionEvent event) {

        String txnId = event.getTxnId();
        String status = event.getTxnStatus();
    }
}