package com.alipay.business.web;

import com.alipay.business.biz.service.impl.auth.JwtClaims;
import com.alipay.business.biz.service.impl.auth.JwtContextHolder;
import com.alipay.business.common.service.facade.result.CommitSessionResponse;
import com.alipay.business.common.service.facade.result.ConfirmUploadResponse;
import com.alipay.business.biz.service.impl.receipt.ReceiptSessionData;
import com.alipay.business.biz.service.impl.receipt.ReceiptService;
import com.alipay.business.biz.service.impl.receipt.ReceiptUploadResult;
import com.alipay.business.biz.service.impl.receipt.SelectItemsRequest;
import com.alipay.business.biz.service.impl.receipt.SessionService;
import com.alipay.business.common.service.facade.result.UploadUrlResponse;
import com.alipay.business.common.service.facade.api.QrCodeService;
import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.common.service.facade.item.SessionItem;
import com.alipay.business.common.service.facade.request.ConfirmUploadRequest;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.core.service.ReceiptItemRepository;
import com.alipay.business.core.service.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ReceiptItemRepository receiptItemRepository;

    @PostMapping("/upload-url")
    public UploadUrlResponse generatePresignedUrl() {
        JwtClaims claims = JwtContextHolder.get();
        return receiptService.generatePresignedUrl(claims.getSubject());
    }

    @PostMapping("/confirm-upload")
    public ConfirmUploadResponse confirmUpload(@RequestBody ConfirmUploadRequest request) {
        JwtClaims claims = JwtContextHolder.get();
        String userId = claims.getSubject();
        // we need to query idempotency keys if it exists.

        // if it exists,return exception that this has already been created
        ReceiptUploadResult uploadResult = receiptService.validateAndPersist(request, userId);

        String sessionId = sessionService.createSession(
                uploadResult.getReceiptId(),
                uploadResult.getReceiptUrl(),
                uploadResult.getItems(),
                userId);

        // only generate the qr token for the display of the QR for joining the session.
        GenerateQrCodeRequest qrRequest = new GenerateQrCodeRequest();
        qrRequest.setUserId(userId);
        qrRequest.setQrIntent(QrIntent.GROUP_RECEIPT.name());
        qrRequest.setSessionId(sessionId);
        qrRequest.setAmount("0");
        qrRequest.setCurrency("SGD");
        qrRequest.setQrType("DYNAMIC");

        String qrToken = qrCodeService.generateQrCode(qrRequest).getResult();

        // store sessionId as referenceId so history page can navigate back to session
        receiptRepository.updateReceiptReferenceId(uploadResult.getReceiptId(), sessionId);

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
        sessionService.updateSelection(sessionId, claims.getSubject(), request.getItemQuantities());
    }

    @PostMapping("/session/{sessionId}/commit")
    public CommitSessionResponse commitSession(@PathVariable String sessionId) {
        JwtClaims claims = JwtContextHolder.get();
        String userId = claims.getSubject();

        ReceiptSessionData session = sessionService.getReceiptSession(sessionId);
        List<SessionItem> myItems = session.getItems().stream()
                .filter(item -> item.getClaims().getOrDefault(userId, 0) > 0)
                .toList();
        if (myItems.isEmpty()) {
            throw new IllegalStateException("No items selected for user " + userId);
        }

        // per-user scoped QR reference: enables lockReceiptItemByQrId to identify this user's items
        String qrReferenceId = userId + ":" + sessionId;


        GenerateQrCodeRequest payQrRequest = new GenerateQrCodeRequest();
        payQrRequest.setUserId(userId);
        payQrRequest.setQrIntent(QrIntent.GROUP_RECEIPT.name());
        payQrRequest.setSessionId(qrReferenceId);
        payQrRequest.setAmount("0");
        payQrRequest.setCurrency("SGD");
        payQrRequest.setQrType("DYNAMIC");

        // TTL token for transfer Init
        String payQrToken = qrCodeService.generateQrCode(payQrRequest).getResult();
        return new CommitSessionResponse(payQrToken);
    }
}
