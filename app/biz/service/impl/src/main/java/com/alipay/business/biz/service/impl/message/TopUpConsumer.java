package com.alipay.business.biz.service.impl.message;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.TransactionCategory;
import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.account_center.common.service.facade.enums.TransactionType;
import com.alipay.account_center.common.service.facade.enums.TxnEventType;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.InsertTransactionRecordRequest;
import com.alipay.account_center.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.business.biz.service.impl.business.TransactionService;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;
import com.alipay.business.common.service.facade.enums.IdempotencyTypeEnum;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

import static com.alipay.business.biz.service.impl.constant.GlobalBizConstants.AUTO_RELOAD;
import static com.alipay.business.biz.service.impl.constant.GlobalBizConstants.STRIPE_CLEARING_ACCOUNT;
import static com.alipay.business.biz.service.impl.constant.GlobalBizConstants.TYPE;

/**
 * @author adam
 * @date 8/4/2026 8:45 AM
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
            return;
        }

        String userId = intent.getMetadata().get("userId");
        String txnId = intent.getMetadata().get("txnId");
        String type = intent.getMetadata().get(TYPE);

        // --- 2. Auto-reload: no pre-existing txnId — create records on the fly ---
        if (txnId == null && AUTO_RELOAD.equals(type)) {
            txnId = bootstrapAutoReload(intent, userId, paymentIntentId);
            if (txnId == null) return;
        }

        // --- 3. Idempotency guard ---
        IdempotencyKeys key = idempotencyKeysRepository.queryIdempotencyKeysByReferenceId(txnId);

        if (key == null) {
            logger.error("Missing idempotency key for txn={}", txnId);
            return;
        }

        if (IdempotencyKeysStatusEnum.SUCCESS.getCode().equals(key.getStatus())) {
            logger.info("Duplicate event for txn={}, skipping", txnId);
            return;
        }

        key.setStatus(IdempotencyKeysStatusEnum.PENDING.getCode());
        idempotencyKeysRepository.updateIdempotencyKeys(key);

        try {
            // --- 4. Fetch account ---
            QueryAccountInfoRequest request = new QueryAccountInfoRequest();
            request.setUserId(userId);

            AccountBizResult<AccountInfoItem> accountInfo =
                    accountServiceClient.queryAccountInfoByUserId(request);

            String accountId = accountInfo.getResult().getAccountId();

            // --- 5. Credit account ---
            transactionService.publishTransfer(accountId, txnId, TxnEventType.TOP_UP.getCode());

            logger.info("Top-up SUCCESS for txnId={}, accountId={}", txnId, accountId);

        } catch (Exception e) {
            logger.error("Top-up failed for PI={}", paymentIntentId, e);
            key.setStatus("FAILED");
            idempotencyKeysRepository.updateIdempotencyKeys(key);
            throw e;
        }
    }

    private String bootstrapAutoReload(PaymentIntent intent, String userId, String paymentIntentId) {
        try {
            BigDecimal amount = BigDecimal.valueOf(intent.getAmount()).divide(BigDecimal.valueOf(100));
            String currency = intent.getCurrency().toUpperCase();

            QueryAccountInfoRequest accountRequest = new QueryAccountInfoRequest();
            accountRequest.setUserId(userId);
            AccountBizResult<AccountInfoItem> accountInfo =
                    accountServiceClient.queryAccountInfoByUserId(accountRequest);

            InsertTransactionRecordRequest insertRequest = new InsertTransactionRecordRequest();
            insertRequest.setPayerAccountNo(STRIPE_CLEARING_ACCOUNT);
            insertRequest.setPayeeAccountNo(accountInfo.getResult().getAccountId());
            insertRequest.setAmount(amount);
            insertRequest.setCurrency(currency);
            insertRequest.setTxnType(TransactionType.TOP_UP);
            insertRequest.setStatus(TransactionStatusEnum.PENDING);
            insertRequest.setCategory(TransactionCategory.TOP_UP);

            AccountBizResult<TransactionRecordItem> txnRecord =
                    accountServiceClient.insertTransactionRecord(insertRequest);
            String txnId = txnRecord.getResult().getTxnId();

            IdempotencyKeys key = new IdempotencyKeys();
            key.setIdempotencyKey(paymentIntentId);
            key.setIdempotencyType(IdempotencyTypeEnum.TOP_UP.getCode());
            key.setUserId(Long.valueOf(userId));
            key.setReferenceId(txnId);
            key.setStatus(IdempotencyKeysStatusEnum.PENDING.getCode());
            key.setRetryCount(0);
            key.setCreatedAt(new Date());
            key.setUpdatedAt(new Date());
            idempotencyKeysRepository.insertIdempotencyKey(key);

            logger.info("Bootstrapped auto-reload record txnId={} for userId={}", txnId, userId);
            return txnId;

        } catch (Exception e) {
            logger.error("Failed to bootstrap auto-reload for PI={}", paymentIntentId, e);
            return null;
        }
    }
}
