package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.business.biz.service.impl.template.BusinessServiceTemplate;
import com.alipay.business.common.service.facade.api.BusinessService;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.common.service.integration.user.UserServiceClient;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractBusinessBizService implements BusinessService {


    protected BusinessServiceTemplate businessServiceTemplate;

    @Autowired
    protected AccountServiceClient accountServiceClient;

    protected IdempotencyKeysRepository idempotencyKeysRepository;

    protected UserServiceClient userServiceClient;

    public void setBusinessServiceTemplate(BusinessServiceTemplate businessServiceTemplate) {
        this.businessServiceTemplate = businessServiceTemplate;
    }

    public void setAccountServiceClient(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    public void setIdempotencyKeysRepository(IdempotencyKeysRepository idempotencyKeysRepository) {
        this.idempotencyKeysRepository = idempotencyKeysRepository;
    }

    public void setUserServiceClient(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }
}
