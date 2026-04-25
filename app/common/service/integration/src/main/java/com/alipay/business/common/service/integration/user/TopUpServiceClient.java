package com.alipay.business.common.service.integration.user;

import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.item.UserCardDetailItem;
import com.alipay.usercenter.common.service.facade.request.QueryDefaultCardRequest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * @author adam
 * @date 24/3/2026 11:17 PM
 */
public interface TopUpServiceClient {

    /**
     * query user info
     * @param request
     * @return
     */
    @POST
    @Path("/queryDefaultCard")
    UserBizResult<UserCardDetailItem> queryDefaultCard(QueryDefaultCardRequest request);

}