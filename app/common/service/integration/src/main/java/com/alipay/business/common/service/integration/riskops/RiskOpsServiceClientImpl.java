package com.alipay.business.common.service.integration.riskops;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.RiskOpsResultCode;
import com.alipay.business.common.service.integration.AbstractServiceClient;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.riskops.common.service.facade.api.RiskOpsService;
import com.alipay.riskops.common.service.facade.baseresult.RiskOpsBizResult;
import com.alipay.riskops.common.service.facade.request.RiskDecisionRequest;
import com.alipay.riskops.common.service.facade.result.RiskDecisionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author adam
 * @date 25/5/2026 11:25 PM
 */
@Service
public class RiskOpsServiceClientImpl extends AbstractServiceClient implements RiskOpsServiceClient {

    @Override
    public RiskOpsBizResult<RiskDecisionResult> evaluateTransferRisk(RiskDecisionRequest riskDecision) {
        AssertUtil.notNull(riskDecision, BusinessResultCode.PARAM_ILLEGAL, "Risk decision request cannot be null");
        RiskOpsBizResult<RiskDecisionResult> result = riskOpsService.evaluateTransferRisk(riskDecision);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, "Risk decision result cannot be null");
        return result;
    }
}
