package com.alipay.business.common.service.integration;

import com.alipay.account_center.common.service.facade.api.AccountService;
import com.alipay.merchant.common.service.facade.api.MerchantService;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;
import com.alipay.usercenter.common.service.facade.api.TopUpService;
import com.alipay.usercenter.common.service.facade.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbstractServiceClient {

    /**
     * account service
     */
    @SofaReference(interfaceType = AccountService.class,
            binding = @SofaReferenceBinding(bindingType = "rest", directUrl = "http://127.0.0.1:8341"),
            jvmFirst = true)
    protected AccountService accountService;

    /**
     * user service
     */
    @SofaReference(interfaceType = UserService.class,
            binding = @SofaReferenceBinding(bindingType = "rest", directUrl = "http://127.0.0.1:8342"),
            jvmFirst = true)
    protected UserService userService;

    /**
     * user service
     */
    @SofaReference(interfaceType = TopUpService.class,
            binding = @SofaReferenceBinding(bindingType = "rest", directUrl = "http://127.0.0.1:8342"),
            jvmFirst = true)
    protected TopUpService topUpService;


    /**
     * merchant service
     */
    @SofaReference(interfaceType = MerchantService.class,
            binding = @SofaReferenceBinding(bindingType = "rest", directUrl = "http://127.0.0.1:8345"),
            jvmFirst = true)
    protected MerchantService merchantService;


}
