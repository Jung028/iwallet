package com.alipay.business.common.service.integration.account;

import com.alipay.alipay_plus.common.service.facade.baseresult.AccountBizResult;
import com.alipay.alipay_plus.common.service.facade.item.AccountInfoItem;
import com.alipay.alipay_plus.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.alipay_plus.common.service.facade.request.TransferRequest;

public interface AccountServiceClient {

    AccountBizResult<String> transfer(TransferRequest request);

    AccountBizResult<AccountInfoItem> queryAccountInfo(QueryAccountInfoRequest request);
}
