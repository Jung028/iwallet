package com.alipay.business.common.service.integration.account;

import com.alipay.alipay_plus.common.service.facade.api.AccountService;
import com.alipay.alipay_plus.common.service.facade.baseresult.AccountBizResult;
import com.alipay.alipay_plus.common.service.facade.item.AccountInfoItem;
import com.alipay.alipay_plus.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.alipay_plus.common.service.facade.request.TransferRequest;

public class AccountServiceClientImpl implements AccountServiceClient {

    /**
    * account service
     */
    private AccountService accountService;


    @Override
    public AccountBizResult<String> transfer(TransferRequest request) {
        return null;
    }

    @Override
    public AccountBizResult<AccountInfoItem> queryAccountInfo(QueryAccountInfoRequest request) {
        return null;
    }
}
