package com.alipay.business.web;

import com.alipay.business.biz.service.impl.business.TopUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author adam
 * @date 26/3/2026 9:19 AM
 */
@RestController
@RequestMapping("/business/topup")
public class TopUpController {

    @Autowired
    private TopUpService topUpService;

    @PostMapping("/webhook/stripe")
    public void handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        // publish top up.
        topUpService.publishTopUp(payload, signature);
    }
}