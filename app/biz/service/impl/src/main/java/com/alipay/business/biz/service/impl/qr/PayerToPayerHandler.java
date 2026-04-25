package com.alipay.business.biz.service.impl.qr;

import com.alipay.business.biz.service.impl.business.impl.AbstractBusinessBizService;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.common.service.facade.result.GenerateQrCodeResult;
import com.alipay.business.core.model.util.AssertUtil;
import org.springframework.stereotype.Component;

/**
 * @author adam
 * @date 24/4/2026 12:03 AM
 */
@Component
public class PayerToPayerHandler extends AbstractBusinessBizService implements QrCodeGeneratorHandler {

    @Override
    public QrIntent getQrIntent() {
        return QrIntent.P2P;
    }

    @Override
    public void validate(GenerateQrCodeRequest request) {
        // validate that the merchant id is not blank
        AssertUtil.notBlank(request.getMerchantId(), BusinessResultCode.PARAM_ILLEGAL, "merchant Id cannot be blank");
//        MerchantInfo userInfo = userInfoRepository.queryUserInfoByUserId(request.getUserId());
//        AssertUtil.notNull(userInfo, BusinessResultCode.PARAM_ILLEGAL, "user not exist");

    }

    @Override
    public GenerateQrCodeResult generateQR(GenerateQrCodeRequest request) throws Exception {
        // P2P - merchant generates QR, user scans QR.
        // but user can also generate QR, user scans QR. there are two types of scan, but the intent
        // is both user sends to another account either merchant or user
        // user <<-->> user -->> merchant
        return new GenerateQrCodeResult("", "", "");
    }
}