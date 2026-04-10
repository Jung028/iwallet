package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.AccountResultCode;
import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.account_center.common.service.facade.enums.TxnEventType;
import com.alipay.account_center.common.service.facade.event.EcTransactionEvent;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.QueryTransactionRecordRequest;
import com.alipay.business.biz.service.impl.business.TransactionService;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.core.model.util.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author adam
 * @date 15/3/2026 4:37 PM
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishTransfer(String payerAccountNo, String txnId, String txnEventType) {
        // fetch and validate the transaction record
        QueryTransactionRecordRequest queryRequest = new QueryTransactionRecordRequest();
        queryRequest.setAccountId(payerAccountNo);
        queryRequest.setTxnId(txnId);
        AccountBizResult<TransactionRecordItem> transactionRecord = accountServiceClient
                .queryTransactionRecord(queryRequest);

        AssertUtil.notNull(transactionRecord, BusinessResultCode.PARAM_ILLEGAL,
                "Transaction record not found");

        // the txn must be PENDING — OTP_OVER_LIMIT is no longer
        // a possible status at this point in the new flow
        AssertUtil.isTrue(transactionRecord.getResult().getTxnStatus().equals(TransactionStatusEnum.PENDING),
                BusinessResultCode.ILLEGAL_STATUS,
                "Transaction status is illegal: " + transactionRecord.getResult().getTxnStatus());

        BigDecimal amount = BigDecimal.valueOf(
                        transactionRecord.getResult().getAmount().doubleValue())
                .setScale(2, RoundingMode.HALF_UP);

        EcTransactionEvent event = new EcTransactionEvent(
                transactionRecord.getResult().getTxnId(),
                transactionRecord.getResult().getPayerAccountId(),
                transactionRecord.getResult().getPayeeAccountId(),
                amount,
                transactionRecord.getResult().getCurrency(),
                txnEventType
        );
        // use payerAccountId as partition key — guarantees ordering per account
        kafkaTemplate.send("EC_TRANSACTION", event.getPayerAccountNo(), event);
        System.out.print("PUBLISH EC_TRANSACTION");

    }
}