package com.alipay.business.biz.service.impl.business;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;

/**
 * @author adam
 * @date 26/3/2026 9:21 AM
 */
public interface TopUpService {
    void publishTopUp(Event event);
}