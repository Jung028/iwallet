package com.alipay.business.biz.service.impl.message;

import com.alipay.business.common.service.facade.event.EcAutoReloadEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author adam
 * @date 26/3/2026 6:46 PM
 */
@Service
public class AutoReloadResultConsumer {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = "EC_AUTO_RELOAD", groupId = "business-center")
    public void onMessage(EcAutoReloadEvent event) {
        simpMessagingTemplate.convertAndSend("topic/autoReload/", event);

    }
}