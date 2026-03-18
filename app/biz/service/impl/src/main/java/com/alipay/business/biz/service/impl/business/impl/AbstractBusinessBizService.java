package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.business.biz.service.impl.auth.JwtUtil;
import com.alipay.business.biz.service.impl.auth.TransferTokenService;
import com.alipay.business.biz.service.impl.template.BusinessServiceTemplate;
import com.alipay.business.common.service.facade.api.BusinessService;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.common.service.integration.user.UserServiceClient;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AbstractBusinessBizService {

    @Autowired
    protected BusinessServiceTemplate businessServiceTemplate;

    @Autowired
    protected TransferTokenService transferTokenService;

    @Autowired
    protected AccountServiceClient accountServiceClient;

    @Autowired
    protected IdempotencyKeysRepository idempotencyKeysRepository;

    @Autowired
    protected UserServiceClient userServiceClient;

    @Autowired
    protected TransactionTemplate transactionTemplate;

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
