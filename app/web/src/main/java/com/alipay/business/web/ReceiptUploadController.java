package com.alipay.business.web;

import com.alipay.business.biz.service.impl.auth.JwtClaims;
import com.alipay.business.biz.service.impl.auth.JwtContextHolder;
import com.alipay.business.biz.service.impl.receipt.ConfirmUploadResponse;
import com.alipay.business.biz.service.impl.receipt.ReceiptSessionData;
import com.alipay.business.biz.service.impl.receipt.ReceiptService;
import com.alipay.business.biz.service.impl.receipt.ReceiptUploadResult;
import com.alipay.business.biz.service.impl.receipt.SelectItemsRequest;
import com.alipay.business.biz.service.impl.receipt.SessionService;
import com.alipay.business.biz.service.impl.receipt.UploadUrlResponse;
import com.alipay.business.common.service.facade.api.QrCodeService;
import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.common.service.facade.request.ConfirmUploadRequest;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private ReceiptService receiptService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private QrCodeService qrCodeService;

    @PostMapping("/upload-url")
    public UploadUrlResponse generatePresignedUrl() {
        JwtClaims claims = JwtContextHolder.get();
        return receiptService.generatePresignedUrl(claims.getSubject());
    }

    @PostMapping("/confirm-upload")
    public ConfirmUploadResponse confirmUpload(@RequestBody ConfirmUploadRequest request) {
        JwtClaims claims = JwtContextHolder.get();
        String userId = claims.getSubject();

        ReceiptUploadResult uploadResult = receiptService.validateAndPersist(request, userId);

        String sessionId = sessionService.createSession(
                uploadResult.getReceiptId(),
                uploadResult.getReceiptUrl(),
                uploadResult.getItems());

        GenerateQrCodeRequest qrRequest = new GenerateQrCodeRequest();
        qrRequest.setUserId(userId);
        qrRequest.setQrIntent(QrIntent.GROUP_RECEIPT.name());
        qrRequest.setSessionId(sessionId);
        qrRequest.setAmount("0");
        qrRequest.setCurrency("SGD");
        qrRequest.setQrType("DYNAMIC");

        String qrToken = qrCodeService.generateQrCode(qrRequest).getResult();

        return new ConfirmUploadResponse(sessionId, qrToken);
    }

    @GetMapping("/session/{sessionId}")
    public ReceiptSessionData getSession(@PathVariable String sessionId) {
        return sessionService.getReceiptSession(sessionId);
    }

    @PostMapping("/session/{sessionId}/select")
    public void selectItems(@PathVariable String sessionId,
                            @RequestBody SelectItemsRequest request) {
        JwtClaims claims = JwtContextHolder.get();
        sessionService.updateSelection(sessionId, claims.getSubject(), request.getItemIds());
    }
}
