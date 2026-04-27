package com.alipay.business.common.service.integration.merchant;

import com.alipay.account_center.common.service.facade.request.*;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.integration.AbstractServiceClient;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.merchant.common.service.facade.baseresult.MerchantBizResult;
import com.alipay.merchant.common.service.facade.item.MerchantInfoItem;
import com.alipay.merchant.common.service.facade.result.QueryMerchantInfoRequest;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceClientImpl extends AbstractServiceClient implements MerchantServiceClient {


    @Override
    public MerchantBizResult<MerchantInfoItem> queryMerchantInfo(QueryMerchantInfoRequest request) {
        AssertUtil.notNull(request, BusinessResultCode.PARAM_ILLEGAL, "Query account info request cannot be null");
        AssertUtil.notBlank(request.getMerchantId(), BusinessResultCode.PARAM_ILLEGAL, "merchant id cannot be blank");
        // set cross invoke
        MerchantBizResult<MerchantInfoItem> result = merchantService.queryMerchantInfo(request);
        AssertUtil.notNull(result, BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.notNull(result.getResult(), BusinessResultCode.PARAM_ILLEGAL, ", result is null");
        AssertUtil.isTrue(result.isSuccess(), BusinessResultCode.PARAM_ILLEGAL, ", result is not success");
        return result;
    }


}
