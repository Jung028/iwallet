package com.alipay.business.web;

import com.alipay.business.biz.service.impl.business.BusinessService;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.request.TransferConfirmRequest;
import com.alipay.business.common.service.facade.request.TransferRequest;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/business/basic")
public class BusinessBasicController {

    @SofaReference
    private BusinessService businessService;

    @PostMapping("/transfer.json")
    public BusinessBizResult<String> transferInit(
            @RequestBody TransferRequest request,
            HttpServletRequest httpServletRequest) {
        // retrieve JWT from header, verify JWT via public key.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // get user id from auth principal
        String userId = auth.getPrincipal().toString();

        return businessService.transfer(request, userId);
    }

    @PostMapping("/transfer/confirm.json")
    public BusinessBizResult<String> transferConfirm(
            @RequestBody TransferConfirmRequest request,
            HttpServletRequest httpServletRequest) {
        // retrieve JWT from header, verify JWT via public key.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // get user id from auth principal
        String userId = auth.getPrincipal().toString();

        return businessService.transferConfirm(request, userId);
    }


    public void setBusinessService(BusinessService businessService) {
        this.businessService = businessService;
    }
}