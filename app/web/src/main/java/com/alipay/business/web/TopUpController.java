package com.alipay.business.web;

import com.alipay.business.biz.service.impl.business.TopUpService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author adam
 * @date 26/3/2026 9:19 AM
 */
@RestController
@RequestMapping("/business/topup")
public class TopUpController {

    @Autowired
    private TopUpService topUpService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader) throws IOException {

        String payload = request.getReader()
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // async → do NOT block webhook thread
        topUpService.publishTopUp(event);

        return ResponseEntity.ok("received");
    }
}