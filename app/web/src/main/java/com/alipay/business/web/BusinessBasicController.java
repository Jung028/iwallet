package com.alipay.business.web;

import com.alipay.business.biz.service.impl.auth.JwtClaims;
import com.alipay.business.biz.service.impl.auth.JwtContextHolder;
import com.alipay.business.common.service.facade.api.BusinessService;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.item.IdempotencyKeysItem;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.common.service.facade.result.BusinessBalanceResult;
import com.alipay.business.common.service.facade.result.BusinessTransactionDetailsResult;
import com.alipay.business.common.service.facade.result.BusinessTransactionHistoryResult;
import com.alipay.business.common.service.facade.result.UpdateIdempotencyKeysResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/business/basic")
public class BusinessBasicController {

    @Autowired
    private BusinessService businessService;
    @PostMapping("/transfer.json")
    public BusinessBizResult<String> transfer(
            @RequestBody TransferRequest request) {
        // retrieve JWT from header, verify JWT via public key.
        JwtClaims claims = JwtContextHolder.get();
        return businessService.transfer(request, claims.getSubject());
    }

    @PostMapping("/transferConfirm.json")
    public BusinessBizResult<String> transferConfirm(
            @RequestBody TransferConfirmRequest request) {
        // retrieve JWT from header, verify JWT via public key.
        JwtClaims claims = JwtContextHolder.get();
        return businessService.transferConfirm(request, claims.getUserId());
    }

    @PostMapping("/queryTransactionDetails.json")
    public BusinessBizResult<BusinessTransactionDetailsResult> queryTransactionDetails(
            @RequestBody BusinessTransactionRecordRequest request) {
        return businessService.queryTransactionDetails(request);
    }

    @PostMapping("/queryTransactionHistory.json")
    public BusinessBizResult<BusinessTransactionHistoryResult> queryTransactionHistory(
            @RequestBody BusinessTransactionHistoryRequest request) {
        return businessService.queryTransactionHistory(request);
    }

    @PostMapping("/queryBalance.json")
    public BusinessBizResult<BusinessBalanceResult> queryBalance(
            @RequestBody BusinessBalanceRequest request) {
        return businessService.queryBalance(request);
    }

    @PostMapping("/updateIdempotencyKeys.json")
    public BusinessBizResult<UpdateIdempotencyKeysResult> updateIdempotencyKeys(
            @RequestBody UpdateIdempotencyKeysRequest request) {
        return businessService.updateIdempotencyKeys(request);
    }

    @PostMapping("/queryIdempotencyKeys.json")
    public BusinessBizResult<IdempotencyKeysItem> queryIdempotencyKeys(
            @RequestBody QueryIdempotencyKeysRequest request) {
        return businessService.queryIdempotencyKeys(request);
    }
}