package com.alipay.business.biz.service.impl.message;

import com.alipay.business.common.service.facade.api.BusinessService;
import com.alipay.business.common.service.facade.event.EcAutoReloadEvent;
import com.alipay.business.common.service.facade.request.ChargeCardRequest;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * @author adam
 * @date 26/3/2026 6:46 PM
 */
@Service
public class AutoReloadResultConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AutoReloadResultConsumer.class);

    @Autowired
    private BusinessService businessService;

    @KafkaListener(topics = "EC_AUTO_RELOAD", groupId = "business-center")
    public void onMessage(EcAutoReloadEvent event) {
        ChargeCardRequest request = new ChargeCardRequest();
        request.setAmount(event.getAmount());
        request.setCurrency(event.getCurrency());

        BusinessBizResult<String> result = businessService.chargeCard(request, event.getUserId());

        if (result.isSuccess()) {
            logger.info("Auto-reload charge initiated userId={} pi={}", event.getUserId(), result.getResult());
        } else {
            logger.warn("Auto-reload charge failed userId={}: {}", event.getUserId(), result.getResult());
        }
    }
}
