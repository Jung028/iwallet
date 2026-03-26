package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.business.biz.service.impl.business.TopUpService;
import com.alipay.business.biz.service.impl.helper.ResponseBuilder;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;
import com.alipay.business.common.service.facade.event.EcTopUpEvent;
import com.alipay.business.common.service.integration.user.UserServiceClient;
import com.alipay.business.common.util.requesthash.HashUtil;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.enums.BusinessActionEnum;
import com.alipay.business.core.model.exception.BusinessException;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import com.alipay.usercenter.common.service.facade.enums.CardIssuer;
import com.alipay.usercenter.common.service.facade.enums.CardStatus;
import com.alipay.usercenter.common.service.facade.enums.CardType;
import com.alipay.usercenter.common.service.facade.enums.Provider;
import com.alipay.usercenter.common.service.facade.request.InsertNewCardRequest;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * @author adam
 * @date 26/3/2026 9:21 AM
 */
public class TopUpServiceImpl implements TopUpService {

    private static final Logger logger = LoggerFactory.getLogger(TopUpServiceImpl.class);

    @Autowired
    private IdempotencyKeysRepository idempotencyKeysRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public void publishTopUp(String payload, String signature) {
        // 1 verify the signature from the topUpInit result, using the webhook and signature
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature,
                    webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.warn("Invalid Stripe webhook signature: {}",
                    e.getMessage());
            throw new BusinessException(BusinessResultCode.INVALID_WEBHOOK_SIGNATURE);
        }

        // ensure payment intent is payment intent success.
        if (!"payment_intent.succeeded".equals(event.getType())) {
            logger.warn("Invalid Stripe webhook signature: {}",
                    event.getType());
            throw new BusinessException(BusinessResultCode.PAYMENT_INTENT_ILLEGAL,
                    "Event not handled");
        }

        PaymentIntent intent = (PaymentIntent) event
                .getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new BusinessException(BusinessResultCode.SYSTEM_EXCEPTION,
                        "failed to deserialize payment_intent"));

        String userId = intent.getMetadata().get("userId");
        boolean saveCard = Boolean.parseBoolean(
                intent.getMetadata().get("saveCard"));
        BigDecimal amount = BigDecimal.valueOf(intent.getAmount())
                .divide(BigDecimal.valueOf(100));
        String currency = intent.getCurrency().toUpperCase();

        // Idempotency guard — Stripe fires webhooks at least once,
        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository
                .queryIdempotencyKeysByReferenceId(intent.getId());
        if (idempotencyKeys!= null) {
            logger.info("Duplicate webhook for pi={}, skipping",
                    intent.getId());
            throw new BusinessException(BusinessResultCode.PARAM_ILLEGAL,
                    "Duplicate webhook for pi : " + intent.getId());
        }

        // 4. Save card token if user requested it
        if (saveCard && intent.getPaymentMethod() != null) {
            try {
                PaymentMethod pm = PaymentMethod.retrieve(intent.getPaymentMethod());

                InsertNewCardRequest insertNewCardRequest = new InsertNewCardRequest();

                insertNewCardRequest.setUserId(userId);
                insertNewCardRequest.setGmtCreate(new Date());
                insertNewCardRequest.setGmtModified(new Date());

                // Provider (always Stripe in this flow)
                insertNewCardRequest.setProvider(Provider.STRIPE);

                // Token + customer mapping
                insertNewCardRequest.setProviderToken(intent.getPaymentMethod());
                insertNewCardRequest.setStripeCustomerId(intent.getCustomer());

                insertNewCardRequest.setLast4(pm.getCard().getLast4());
                insertNewCardRequest.setCardIssuer(
                        CardIssuer.valueOf(pm.getCard().getBrand().toUpperCase())
                );
                insertNewCardRequest.setExpiryMonth(pm.getCard().getExpMonth().intValue());
                insertNewCardRequest.setExpiryYear(pm.getCard().getExpYear().intValue());
                insertNewCardRequest.setCardHolderName(pm.getBillingDetails() != null
                        ? pm.getBillingDetails().getName()
                        : null);
                if (pm.getCard().getFunding() != null) {
                    switch (pm.getCard().getFunding()) {
                        case "DEBIT":
                            insertNewCardRequest.setCardType(CardType.DEBIT);
                            break;
                        case "CREDIT":
                            insertNewCardRequest.setCardType(CardType.CREDIT);
                            break;
                        default:
                            throw new IllegalStateException("Illegal card type");
                    }
                } else {
                    insertNewCardRequest.setCardType(CardType.CREDIT);
                }
                insertNewCardRequest.setCardStatus(CardStatus.ACTIVE);
                insertNewCardRequest.setDefault(true);
                userServiceClient.insertNewCard(insertNewCardRequest);
            } catch (StripeException e) {
                // non-fatal — top-up still proceeds
                logger.warn("Failed to save card for userId={}", userId, e);
            }
        }

        // 5. Mark idempotency before publishing — prevents double credit
        IdempotencyKeys beforePublishKey = new IdempotencyKeys();
        beforePublishKey.setIdempotencyKey(intent.getId());
        beforePublishKey.setUserId(Long.valueOf(userId));
        try {
            beforePublishKey.setRequestHash(HashUtil.
                    generateIdempotentRequestHash(intent.getAmount(), intent.getCurrency(),
                            intent.getCustomer()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        beforePublishKey.setStatus(IdempotencyKeysStatusEnum.PENDING);
        beforePublishKey.setRetryCount(0);
        beforePublishKey.setCreatedAt(new Date());
        beforePublishKey.setUpdatedAt(new Date());
        idempotencyKeysRepository.insertIdempotencyKey(beforePublishKey);

        // 6. Publish EC_TOPUP_RECEIVED → AccountCenter credits balance
        //    Same Kafka pipeline as EC_TRANSACTION_RESULT
        EcTopUpEvent topUpEvent = new EcTopUpEvent();
        topUpEvent.setUserId(userId);
        topUpEvent.setAmount(amount);
        topUpEvent.setCurrency(currency);
        topUpEvent.setPaymentIntentId(intent.getId());
        topUpEvent.setGmtTaskOccur(System.currentTimeMillis());

        kafkaTemplate.send("EC_TOPUP_RECEIVED", topUpEvent);

    }
}