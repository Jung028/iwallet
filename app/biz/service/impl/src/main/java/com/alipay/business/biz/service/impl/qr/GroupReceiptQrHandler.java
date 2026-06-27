package com.alipay.business.biz.service.impl.qr;

import com.alipay.business.common.service.facade.enums.OwnerType;
import com.alipay.business.common.service.facade.enums.QrCodeStatus;
import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.common.service.facade.enums.QrType;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.core.model.domain.QrCode;
import com.alipay.usercenter.common.service.facade.exception.UserBizException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

@Component
public class GroupReceiptQrHandler extends AbstractQrHandlerService {

    @Override
    public QrIntent getQrIntent() {
        return QrIntent.GROUP_RECEIPT;
    }

    @Override
    public void validate(GenerateQrCodeRequest request) {
        if (!StringUtils.hasText(request.getSessionId())) {
            throw new IllegalArgumentException("sessionId is required for GROUP_RECEIPT QR");
        }
        if (!StringUtils.hasText(request.getUserId())) {
            throw new IllegalArgumentException("userId is required for GROUP_RECEIPT QR");
        }
    }

    @Override
    public String getOwnerId(GenerateQrCodeRequest request) {
        return request.getUserId();
    }

    @Override
    public OwnerType getOwnerType() {
        return OwnerType.USER;
    }

    @Override
    public String generateQR(GenerateQrCodeRequest request) {
        QrCode qrCode = new QrCode();
        try {
            // Use sessionId as qrId so the payer can recover the session from the JWT.
            qrCode.setQrId(request.getSessionId());
            qrCode.setAmount(BigDecimal.ZERO);
            qrCode.setCurrency("SGD");
            qrCode.setIntent(getQrIntent().getCode());
            qrCode.setCreatedAt(new Date());
            qrCode.setStatus(QrCodeStatus.INIT.getCode());
            qrCode.setOwnerId(getOwnerId(request));
            qrCode.setOwnerType(getOwnerType().getCode());
            qrCode.setUpdatedAt(new Date());
            qrCode.setExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 * 1000L));
            qrCode.setQrType(QrType.DYNAMIC.getCode());
            qrCodeRepository.insertQrCode(qrCode);
        } catch (DuplicateKeyException e) {
            throw new UserBizException(e.toString(), "Qr already exists for this session");
        }

        String qrToken = qrTokenService.issueQrToken(
                qrCode.getQrId(),
                qrCode.getAmount(),
                qrCode.getCurrency(),
                getOwnerId(request),
                getQrIntent().getCode(),
                qrCode.getExpiresAt());

        qrCodeRepository.updateQrCode(qrCode.getQrId(), QrCodeStatus.ACTIVE.getCode());
        return qrToken;
    }
}
