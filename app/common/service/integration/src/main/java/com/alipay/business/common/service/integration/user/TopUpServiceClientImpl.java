package com.alipay.business.common.service.integration.user;

import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.integration.AbstractServiceClient;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.usercenter.common.service.facade.api.TopUpService;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.item.UserCardDetailItem;
import com.alipay.usercenter.common.service.facade.request.QueryDefaultCardRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author adam
 * @date 24/3/2026 11:18 PM
 */
@Service
public class TopUpServiceClientImpl extends AbstractServiceClient implements TopUpServiceClient{

    @Override
    public UserBizResult<UserCardDetailItem> queryDefaultCard(QueryDefaultCardRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, ", verify user auth request is null");
        AssertUtil.notBlank(request.getUserId(), BusinessResultCode.PARAM_ILLEGAL, ", userId cannot be blank");

        UserBizResult<UserCardDetailItem> result = topUpService.queryDefaultCard(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
        return result;
    }
}