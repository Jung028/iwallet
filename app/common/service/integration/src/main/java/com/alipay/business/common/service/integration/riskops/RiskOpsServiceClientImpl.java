package com.alipay.business.common.service.integration.riskops;

import com.alipay.business.common.service.facade.enums.RiskOpsResultCode;
import com.alipay.business.core.model.util.AssertUtil;

/**
 * @author adam
 * @date 25/5/2026 11:25 PM
 */
public class RiskOpsServiceClientImpl implements RiskOpsServiceClient {

    @Override
    public RiskDecision evaluateTransferRisk(RiskDecision riskDecision) {
        AssertUtil.notNull(riskDecision, RiskOpsResultCode.PARAM_ILLEGAL, "RiskDecision can not be null");
        return null;
    }
}