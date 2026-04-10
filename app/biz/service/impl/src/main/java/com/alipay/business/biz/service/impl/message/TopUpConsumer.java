package com.alipay.business.biz.service.impl.message;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.TxnEventType;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.business.biz.service.impl.business.TransactionService;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author adam
 * @date 8/4/2026 8:45 AM
 */
@Component
public class TopUpConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TopUpConsumer.class);

    @Autowired
    private IdempotencyKeysRepository idempotencyKeysRepository;

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Autowired
    private TransactionService transactionService;

    @KafkaListener(topics = "TOP_UP_SUCCESS", groupId = "topup-group")
    public void consume(String paymentIntentId) {

        logger.info("Processing top-up for PI={}", paymentIntentId);

        // --- 1. Fetch from Stripe (authoritative) ---
        PaymentIntent intent;
        try {
            intent = PaymentIntent.retrieve(paymentIntentId);
        } catch (Exception e) {
            logger.error("Failed to fetch PaymentIntent {}", paymentIntentId, e);
            return; // retry via Kafka
        }

        String userId = intent.getMetadata().get("userId");
        String txnId = intent.getMetadata().get("txnId");
        System.out.print(txnId);

        // --- 2. Idempotency guard ---
        IdempotencyKeys key = idempotencyKeysRepository
                .queryIdempotencyKeysByReferenceId(txnId);

        if (key == null) {
            System.out.print(txnId);
            logger.error("Missing idempotency key for txn={}", txnId);
            return;
        }

        if (IdempotencyKeysStatusEnum.SUCCESS.getCode().equals(key.getStatus())) {
            logger.info("Duplicate event for txn={}, skipping", txnId);
            return;
        }

        // move to PROCESSING
        key.setStatus(IdempotencyKeysStatusEnum.PENDING.getCode());
        idempotencyKeysRepository.updateIdempotencyKeys(key);

        try {
            // --- 3. Fetch account ---
            QueryAccountInfoRequest request = new QueryAccountInfoRequest();
            request.setUserId(userId);

            AccountBizResult<AccountInfoItem> accountInfo =
                    accountServiceClient.queryAccountInfoByUserId(request);

            String accountId = accountInfo.getResult().getAccountId();

            // --- 4. Execute transfer ---
            transactionService.publishTransfer(accountId, txnId, TxnEventType.TOP_UP.getCode());

            logger.info("Top-up SUCCESS for txnId={}, accountId={}", txnId, accountId);

        } catch (Exception e) {
            logger.error("Top-up failed for PI={}", paymentIntentId, e);
            System.out.print(e.getMessage());
            System.out.print(paymentIntentId);
            key.setStatus("FAILED");
            idempotencyKeysRepository.updateIdempotencyKeys(key);

            throw e; // let Kafka retry
        }
    }
}