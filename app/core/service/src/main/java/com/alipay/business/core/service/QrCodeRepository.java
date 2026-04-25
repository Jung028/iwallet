package com.alipay.business.core.service;

import com.alipay.business.core.model.domain.QrCode;
import org.springframework.stereotype.Repository;

/**
 * @author adam
 * @date 24/4/2026 12:41 AM
 */
@Repository
public interface QrCodeRepository {
    void insertQrCode(QrCode qrCode);

    void updateQrCode(String qrId, String status);
}