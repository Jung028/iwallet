package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.business.biz.service.impl.business.TopUpService;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;
import com.alipay.business.common.service.facade.event.EcTopUpEvent;
import com.alipay.business.common.service.integration.user.UserServiceClient;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.exception.BusinessException;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.alipay.business.biz.service.impl.constant.GlobalBizConstants.*;

/**
 * @author adam
 * @date 26/3/2026 9:21 AM
 */
@Service
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
        System.out.print("START PUBLISH TOP UP");
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
        if (!PAYMENT_INTENT_SUCCESS.equals(event.getType())) {
            logger.warn("Invalid Stripe webhook signature: {}",
                    event.getType());
            throw new BusinessException(BusinessResultCode.PAYMENT_INTENT_ILLEGAL,
                    "Event not handled");
        }

        PaymentIntent intent = (PaymentIntent) event
                .getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new BusinessException(BusinessResultCode.SYSTEM_EXCEPTION,
                        "failed to deserialize payment_intent"));

        String userId = intent.getMetadata().get(USER_ID);
        BigDecimal amount = BigDecimal.valueOf(intent.getAmount())
                .divide(BigDecimal.valueOf(100));
        String currency = intent.getCurrency().toUpperCase();

        // Idempotency guard — Stripe fires webhooks at least once,
        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository
                .queryIdempotencyKeysByReferenceId(intent.getId());
        if (!idempotencyKeys.getStatus().equals(IdempotencyKeysStatusEnum.PENDING.toString())) {
            logger.info("Duplicate webhook for pi={}, skipping",
                    intent.getId());
            throw new BusinessException(BusinessResultCode.PARAM_ILLEGAL,
                    "Duplicate webhook for pi : " + intent.getId());
        }

        // 5. Mark idempotency before publishing — prevents double credit
        idempotencyKeys.setStatus(String.valueOf(IdempotencyKeysStatusEnum.PENDING));
        idempotencyKeysRepository.updateIdempotencyKeys(idempotencyKeys);

        // 6. Publish EC_TOPUP_RECEIVED → AccountCenter credits balance
        EcTopUpEvent topUpEvent = new EcTopUpEvent();
        topUpEvent.setUserId(userId);
        topUpEvent.setAmount(amount);
        topUpEvent.setCurrency(currency);
        topUpEvent.setPaymentIntentId(intent.getId());
        topUpEvent.setGmtTaskOccur(System.currentTimeMillis());

        kafkaTemplate.send("EC_TOPUP_RECEIVED", topUpEvent);

    }
}