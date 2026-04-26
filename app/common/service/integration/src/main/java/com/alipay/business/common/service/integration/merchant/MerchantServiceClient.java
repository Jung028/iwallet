package com.alipay.business.common.service.integration.merchant;

import com.alipay.account_center.common.service.facade.request.*;
import com.alipay.merchant.common.service.facade.baseresult.MerchantBizResult;
import com.alipay.merchant.common.service.facade.item.MerchantInfoItem;
import com.alipay.merchant.common.service.facade.result.QueryMerchantInfoRequest;
import org.springframework.stereotype.Service;

@Service
public interface MerchantServiceClient {

    MerchantBizResult<MerchantInfoItem> queryMerchantInfo(QueryMerchantInfoRequest queryMerchantInfoRequest);

}
