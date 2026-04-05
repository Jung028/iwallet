package com.alipay.business.common.service.integration.user;

import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.item.UserCardDetailItem;
import com.alipay.usercenter.common.service.facade.request.QueryDefaultCardRequest;
import org.springframework.stereotype.Service;

/**
 * @author adam
 * @date 24/3/2026 11:18 PM
 */
@Service
public class TopUpServiceClientImpl implements TopUpServiceClient{

    @Override
    public UserBizResult<UserCardDetailItem> queryDefaultCard(QueryDefaultCardRequest request) {
        return null;
    }
}