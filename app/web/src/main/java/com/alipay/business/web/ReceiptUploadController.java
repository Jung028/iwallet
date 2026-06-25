package com.alipay.business.web;

import com.alipay.business.biz.service.impl.auth.JwtClaims;
import com.alipay.business.biz.service.impl.auth.JwtContextHolder;
import com.alipay.business.biz.service.impl.receipt.ReceiptFileMetadata;
import com.alipay.business.biz.service.impl.receipt.ReceiptUploadService;
import com.alipay.business.biz.service.impl.receipt.UploadUrlResponse;
import com.alipay.business.common.service.facade.request.ConfirmUploadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Group payment feature. Allows multiple users to live interact and select the order
 * in the receipt to pay for that specific order.
 */
@RestController
@RequestMapping("/wallet/receipt")
public class ReceiptUploadController {

    @Autowired
    private ReceiptUploadService receiptUploadService;

    @PostMapping("/generatePresignedUrl")
    public UploadUrlResponse generatePresignedUrl() {
        JwtClaims claims = JwtContextHolder.get();
        return receiptUploadService.generatePresignedUrl(claims.getSubject());
    }

    @PostMapping("/validateAndPersist")
    public ReceiptFileMetadata validateAndPersist(@RequestBody ConfirmUploadRequest request) {
        JwtClaims claims = JwtContextHolder.get();
        return receiptUploadService.validateAndPersist(request, claims.getSubject());
    }
}
