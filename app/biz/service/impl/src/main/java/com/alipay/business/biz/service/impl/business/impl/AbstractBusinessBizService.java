package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.business.biz.service.impl.business.BusinessService;
import com.alipay.business.biz.service.impl.template.BusinessServiceTemplate;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.usercenter.common.service.facade.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractBusinessBizService implements BusinessService {


    protected BusinessServiceTemplate businessServiceTemplate;

    @SofaReference
    protected UserService userService;

    @Autowired
    protected AccountServiceClient accountServiceClient;

    protected IdempotencyKeysRepository idempotencyKeysRepository;

    public void setBusinessServiceTemplate(BusinessServiceTemplate businessServiceTemplate) {
        this.businessServiceTemplate = businessServiceTemplate;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setAccountServiceClient(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    public void setIdempotencyKeysRepository(IdempotencyKeysRepository idempotencyKeysRepository) {
        this.idempotencyKeysRepository = idempotencyKeysRepository;
    }
}
