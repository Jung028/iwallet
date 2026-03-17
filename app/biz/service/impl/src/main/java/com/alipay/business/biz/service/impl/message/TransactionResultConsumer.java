package com.alipay.business.biz.service.impl.message;

import com.alipay.account_center.common.service.facade.event.EcTransactionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author adam
 * @date 15/3/2026 5:08 PM
 */
@Service
public class TransactionResultConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "EC_TRANSACTION_RESULT", groupId = "business-center")
    public void onTransactionResult(EcTransactionEvent event) {
        // push directly to the user's private channel using their txnId
        System.out.println(event.getTxnStatus());
        messagingTemplate.convertAndSend(
                "/topic/transaction/" + event.getTxnId(),
                event
        );
    }
}