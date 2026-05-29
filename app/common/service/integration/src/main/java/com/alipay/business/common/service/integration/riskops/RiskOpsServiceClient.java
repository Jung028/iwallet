package com.alipay.business.common.service.integration.riskops;

/**
 * @author adam
 * @date 25/5/2026 9:11 PM
 */
public interface RiskOpsServiceClient {
    RiskDecision evaluateTransferRisk(RiskDecision riskDecision);
}