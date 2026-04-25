package com.alipay.business.web;

import com.alipay.business.biz.service.impl.auth.JwtClaims;
import com.alipay.business.biz.service.impl.auth.JwtContextHolder;
import com.alipay.business.common.service.facade.api.QrCodeService;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.common.service.facade.result.GenerateQrCodeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author adam
 * @date 25/4/2026 2:09 PM
 */
@RestController
@RequestMapping("/business/qr")
public class QrController {

    @Autowired
    private QrCodeService qrCodeService;

    @PostMapping("/generateQrCode.json")
    public BusinessBizResult<GenerateQrCodeResult> generateQrCode(@RequestBody GenerateQrCodeRequest request) {
        try {
            JwtClaims claims = JwtContextHolder.get();
            request.setUserId(claims.getSubject());
            // should this be able to change user login password as well?
            return qrCodeService.generateQrCode(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}