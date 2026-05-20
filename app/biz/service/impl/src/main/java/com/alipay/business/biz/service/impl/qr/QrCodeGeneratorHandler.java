package com.alipay.business.biz.service.impl.qr;


import com.alipay.business.common.service.facade.enums.OwnerType;
import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;

/**
 * @author adam
 * @date 23/4/2026 11:53 PM
 */
public interface QrCodeGeneratorHandler {

    /**
     * get qr intent
     * @return
     */
    QrIntent getQrIntent();

    /**
     * used to validate either merchant or user exists.
     */
    void validate(GenerateQrCodeRequest request);

    /**
     * get owner id, either user or merchant
     * @param request
     * @return
     */
    String getOwnerId(GenerateQrCodeRequest request);

    /**
     * get owner type, either merchant or user
     * @return
     */
    OwnerType getOwnerType();

    /**
     * used to generate QR code.
     * @param request
     */
    String generateQR(GenerateQrCodeRequest request);
}