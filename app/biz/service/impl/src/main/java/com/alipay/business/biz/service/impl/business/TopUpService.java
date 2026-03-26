package com.alipay.business.biz.service.impl.business;

/**
 * @author adam
 * @date 26/3/2026 9:21 AM
 */
public interface TopUpService {
    void publishTopUp(String payload, String signature);
}