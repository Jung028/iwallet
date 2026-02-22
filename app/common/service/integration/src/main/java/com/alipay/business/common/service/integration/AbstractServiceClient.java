package com.alipay.business.common.service.integration;

import com.alipay.alipay_plus.common.service.facade.api.AccountService;
import com.alipay.usercenter.common.service.facade.api.UserService;

public class AbstractServiceClient {

    /**
     * account service
     */
    protected AccountService accountService;

    /**
     * user service
     */
    protected UserService userService;

    /**
     * set account service
     * @param accountService
     */
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * set user service
     * @param userService
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
