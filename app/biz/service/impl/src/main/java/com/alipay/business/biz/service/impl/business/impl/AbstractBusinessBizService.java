package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.business.biz.service.impl.auth.QrTokenService;
import com.alipay.business.biz.service.impl.auth.TransferTokenService;
import com.alipay.business.biz.service.impl.business.TransactionService;
import com.alipay.business.biz.service.impl.template.BusinessServiceTemplate;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.common.service.integration.merchant.MerchantServiceClient;
import com.alipay.business.common.service.integration.user.TopUpServiceClient;
import com.alipay.business.common.service.integration.user.UserServiceClient;
import com.alipay.business.core.service.IdempotencyKeysRepository;
import com.alipay.business.core.service.QrCodeRepository;
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
    protected TopUpServiceClient topUpServiceClient;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Autowired
    protected TransactionService transactionService;

    @Autowired
    protected QrTokenService qrTokenService;

    @Autowired
    protected MerchantServiceClient merchantServiceClient;
}
