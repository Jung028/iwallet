package com.alipay.business.biz.service.impl.qr;

import com.alipay.business.biz.service.impl.business.impl.AbstractBusinessBizService;
import com.alipay.business.biz.service.impl.qr.utils.SignUtils;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.OwnerType;
import com.alipay.business.common.service.facade.enums.QrCodeStatus;
import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.common.service.facade.result.GenerateQrCodeResult;
import com.alipay.business.core.model.domain.QrCode;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.usercenter.common.service.facade.enums.UserResultCode;
import com.alipay.usercenter.common.service.facade.exception.UserBizException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;


/**
 * @author adam
 * @date 24/4/2026 12:03 AM
 */
@Component
public class PayerToMerchantHandler extends AbstractBusinessBizService implements QrCodeGeneratorHandler {

    private static final String KEY = "#";

    @Override
    public QrIntent getQrIntent() {
        return QrIntent.P2M;
    }

    @Override
    public void validate(GenerateQrCodeRequest request) {
        // validate that the merchant id is not blank
        // P2M - user generates QR, merchant scans QR, the money flows from
        // user to the merchant. not allowed for user -->> user.
        AssertUtil.notBlank(request.getMerchantId(), BusinessResultCode.PARAM_ILLEGAL, "merchant Id cannot be blank");
        // should query merchant info by merchant id.
//        MerchantInfo merchantInfo = merchantInfoRepository.queryMerchantInfoByMerchantId(request.getMerchantId());
        
//        UserInfo userInfo = userInfoRepository.queryUserInfoByUserId(request.getUserId());
//        AssertUtil.notNull(userInfo, BusinessResultCode.PARAM_ILLEGAL, "user not exist");

    }

    @Override
    public GenerateQrCodeResult generateQR(GenerateQrCodeRequest request) {
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
            qrCode.setOwnerId(request.getMerchantId());
            qrCode.setOwnerType(OwnerType.MERCHANT.getCode());
            qrCode.setUpdatedAt(new Date());
            // Set expiry to 1 minute, then QR is expired.
            qrCode.setExpiresAt(new Date(System.currentTimeMillis() + 60 * 1000));

            qrCodeRepository.insertQrCode(qrCode);
        } catch (DuplicateKeyException e) {
            throw new UserBizException(e.toString(), "Qr already exists");
        }

        // then sign it,
        String payload = qrCode.getQrId()
                + "|" + qrCode.getAmount()
                + "|" + qrCode.getCurrency()
                + "|" + request.getMerchantId();

        String signature = SignUtils.sign(payload, KEY);

        // store it
        qrCode.setSignature(signature);

        // the update status to ACTIVE
        qrCodeRepository.updateQrCode(qrCode.getQrId(), QrCodeStatus.ACTIVE.getCode());

        // return the signature for the qr to be return to frontend.
        return new GenerateQrCodeResult(qrCode.getQrId(), payload, qrCode.getSignature());
    }
}