package com.alipay.business.biz.service.impl.qr;

import com.alipay.business.biz.service.impl.auth.QrTokenService;
import com.alipay.business.common.service.facade.enums.QrCodeStatus;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.common.service.integration.merchant.MerchantServiceClient;
import com.alipay.business.common.service.integration.user.UserServiceClient;
import com.alipay.business.core.model.domain.QrCode;
import com.alipay.business.core.service.QrCodeRepository;
import com.alipay.usercenter.common.service.facade.exception.UserBizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * @author adam
 * @date 25/4/2026 7:19 PM
 */
public abstract class AbstractQrHandlerService implements QrCodeGeneratorHandler{

    protected static final String KEY = "";

    @Autowired
    protected QrCodeRepository qrCodeRepository;

    @Autowired
    protected UserServiceClient userServiceClient;

    @Autowired
    protected MerchantServiceClient merchantServiceClient;

    @Autowired
    protected QrTokenService qrTokenService;

    @Override
    public String generateQR(GenerateQrCodeRequest request) {
        // first check if it exists first, then only try insert, idempotency.
        // insert QR code.
        QrCode qrCode = new QrCode();
        try {
            // set expiry time, signature, qr_id, currency, amount, receiver_id, into a QR instance
            qrCode.setQrId(UUID.randomUUID().toString());
            qrCode.setAmount(BigDecimal.valueOf(Long.parseLong(request.getAmount())));
            qrCode.setCurrency(request.getCurrency());
            qrCode.setIntent(request.getQrIntent());
            qrCode.setCreatedAt(new Date());
            qrCode.setStatus(QrCodeStatus.INIT.getCode());
            qrCode.setOwnerId(getOwnerId(request));
            qrCode.setOwnerType(getOwnerType().getCode());
            qrCode.setUpdatedAt(new Date());
            // Set expiry to 1 minute, then QR is expired.
            qrCode.setExpiresAt(new Date(System.currentTimeMillis() + 60 * 1000));

            qrCodeRepository.insertQrCode(qrCode);
        } catch (DuplicateKeyException e) {
            throw new UserBizException(e.toString(), "Qr already exists");
        }

        // Build payload differently depending on the QR intent
        String qrToken = qrTokenService.issueQrToken(
                qrCode.getQrId(),
                qrCode.getAmount(),
                qrCode.getCurrency(),
                getOwnerId(request),
                getQrIntent().getCode(),
                qrCode.getExpiresAt());

        // the update status to ACTIVE
        qrCodeRepository.updateQrCode(qrCode.getQrId(), QrCodeStatus.ACTIVE.getCode());

        // return the token, so that when the user scans this token, transferInit can verify it.
        return qrToken;
    }
}