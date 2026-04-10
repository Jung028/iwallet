package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.TxnEventType;
import com.alipay.account_center.common.service.facade.event.EcTransactionEvent;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.business.biz.service.impl.business.TopUpService;
import com.alipay.business.biz.service.impl.business.TransactionService;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.common.service.integration.user.UserServiceClient;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.exception.BusinessException;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
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

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Autowired
    private TransactionService transactionService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    /**
     * Process Stripe webhook event for top-up
     */
    public void publishTopUp(Event event) {


        // only care about success event
        if (!"payment_intent.succeeded".equals(event.getType())) {
            return;
        }
        System.out.print(event.getType());
        System.out.print(event.getId());

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        StripeObject stripeObject = deserializer.getObject()
                .orElseGet(() -> {
                    try {
                        return deserializer.deserializeUnsafe();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize Stripe object", e);
                    }
                });

        if (!(stripeObject instanceof PaymentIntent intent)) {
            throw new IllegalStateException("Event is not PaymentIntent");
        }

        String paymentIntentId = intent.getId(); // ✅ pi_xxx
        // push to Kafka (event-driven)
        kafkaTemplate.send("TOP_UP_SUCCESS", paymentIntentId);

        logger.info("Enqueued top-up event for PI={}", paymentIntentId);
    }
}