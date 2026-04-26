package com.alipay.business.biz.service.impl.qr;


import com.alipay.business.common.service.facade.enums.QrIntent;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.common.service.facade.result.GenerateQrCodeResult;

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
     * used to generate QR code.
     * @param request
     */
    GenerateQrCodeResult generateQR(GenerateQrCodeRequest request) throws Exception;
}