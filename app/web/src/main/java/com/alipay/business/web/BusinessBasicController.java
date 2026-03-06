package com.alipay.business.web;

import com.alipay.business.common.service.facade.api.BusinessService;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.item.IdempotencyKeysItem;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.common.service.facade.result.BusinessBalanceResult;
import com.alipay.business.common.service.facade.result.BusinessTransactionDetailsResult;
import com.alipay.business.common.service.facade.result.BusinessTransactionHistoryResult;
import com.alipay.business.common.service.facade.result.UpdateIdempotencyKeysResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/business/basic")
public class BusinessBasicController {

    private BusinessService businessService;

    @PostMapping("/transfer.json")
    public BusinessBizResult<String> transfer(
            @RequestBody TransferRequest request,
            HttpServletRequest httpServletRequest) {
        // retrieve JWT from header, verify JWT via public key.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // get user id from auth principal
        String userId = auth.getPrincipal().toString();

        return businessService.transfer(request, userId);
    }

    @PostMapping("/transferConfirm.json")
    public BusinessBizResult<String> transferConfirm(
            @RequestBody TransferConfirmRequest request,
            HttpServletRequest httpServletRequest) {
        // retrieve JWT from header, verify JWT via public key.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // get user id from auth principal
        String userId = auth.getPrincipal().toString();

        return businessService.transferConfirm(request, userId);
    }

    @PostMapping("/queryTransactionDetails.json")
    public BusinessBizResult<BusinessTransactionDetailsResult> queryTransactionDetails(
            @RequestBody BusinessTransactionRecordRequest request,
            HttpServletRequest httpServletRequest) {
        // retrieve JWT from header, verify JWT via public key.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // get user id from auth principal
        String userId = auth.getPrincipal().toString();

        return businessService.queryTransactionDetails(request);
    }

    @PostMapping("/queryTransactionHistory.json")
    public BusinessBizResult<BusinessTransactionHistoryResult> queryTransactionHistory(
            @RequestBody BusinessTransactionHistoryRequest request,
            HttpServletRequest httpServletRequest) {
        // retrieve JWT from header, verify JWT via public key.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // get user id from auth principal
        String userId = auth.getPrincipal().toString();

        return businessService.queryTransactionHistory(request);
    }

    @PostMapping("/queryBalance.json")
    public BusinessBizResult<BusinessBalanceResult> queryBalance(
            @RequestBody BusinessBalanceRequest request,
            HttpServletRequest httpServletRequest) {
        // retrieve JWT from header, verify JWT via public key.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // get user id from auth principal
        String userId = auth.getPrincipal().toString();

        return businessService.queryBalance(request);
    }

    @PostMapping("/updateIdempotencyKeys.json")
    public BusinessBizResult<UpdateIdempotencyKeysResult> updateIdempotencyKeys(
            @RequestBody UpdateIdempotencyKeysRequest request,
            HttpServletRequest httpServletRequest) {
        // retrieve JWT from header, verify JWT via public key.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // get user id from auth principal
        String userId = auth.getPrincipal().toString();

        return businessService.updateIdempotencyKeys(request);
    }

    @PostMapping("/queryIdempotencyKeys.json")
    public BusinessBizResult<IdempotencyKeysItem> queryIdempotencyKeys(
            @RequestBody QueryIdempotencyKeysRequest request,
            HttpServletRequest httpServletRequest) {
        // retrieve JWT from header, verify JWT via public key.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // get user id from auth principal
        String userId = auth.getPrincipal().toString();

        return businessService.queryIdempotencyKeys(request);
    }

    public void setBusinessService(BusinessService businessService) {
        this.businessService = businessService;
    }
}