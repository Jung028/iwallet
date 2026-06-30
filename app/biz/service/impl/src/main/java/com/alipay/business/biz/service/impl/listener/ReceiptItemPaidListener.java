package com.alipay.business.biz.service.impl.listener;

import com.alipay.business.biz.service.impl.event.ReceiptItemPaidEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author adam
 * @date 30/6/2026 11:14 AM
 */

@Component
public class ReceiptItemPaidListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReceiptItemPaid(ReceiptItemPaidEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/receipt/" + event.getReceiptItem().getItemId(),
                event.getReceiptItem()
        );
    }
}