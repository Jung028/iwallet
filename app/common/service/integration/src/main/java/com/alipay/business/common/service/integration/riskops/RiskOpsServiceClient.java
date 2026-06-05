package com.alipay.business.common.service.integration.riskops;

import com.alipay.riskops.common.service.facade.baseresult.RiskOpsBizResult;
import com.alipay.riskops.common.service.facade.request.RiskDecisionRequest;
import com.alipay.riskops.common.service.facade.result.RiskDecisionResult;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * @author adam
 * @date 25/5/2026 9:11 PM
 */
public interface RiskOpsServiceClient {

    @POST
    @Path("/evaluateTransferRisk")
    RiskOpsBizResult<RiskDecisionResult> evaluateTransferRisk(RiskDecisionRequest riskDecision);
}