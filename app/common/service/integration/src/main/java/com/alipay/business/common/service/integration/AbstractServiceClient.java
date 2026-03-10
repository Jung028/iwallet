package com.alipay.business.common.service.integration;

import com.alipay.alipay_plus.common.service.facade.api.AccountService;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;
import com.alipay.usercenter.common.service.facade.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbstractServiceClient {

    /**
     * account service
     */
    @SofaReference(interfaceType = AccountService.class,
            binding = @SofaReferenceBinding(bindingType = "bolt", directUrl = "bolt://127.0.0.1:12200"),
            jvmFirst = true)
    protected AccountService accountService;

    /**
     * user service
     */
    @SofaReference(interfaceType = UserService.class,
            binding = @SofaReferenceBinding(bindingType = "rest", directUrl = "http://127.0.0.1:8341"),
            jvmFirst = true)
    protected UserService userService;

}
