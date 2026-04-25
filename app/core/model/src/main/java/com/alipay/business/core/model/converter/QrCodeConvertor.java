package com.alipay.business.core.model.converter;


import com.alipay.business.common.dal.auto.dataobject.QrCodeDO;
import com.alipay.business.core.model.domain.QrCode;

/**
 * @author adam
 * @date 25/4/2026 1:00 PM
 */
public class QrCodeConvertor {

    public static QrCodeDO convertToDO(QrCode qrCode) {
        QrCodeDO qrCodeDO = new QrCodeDO();
        qrCodeDO.setAmount(qrCode.getAmount());
        qrCodeDO.setCurrency(qrCode.getCurrency());
        qrCodeDO.setQrId(qrCode.getQrId());
        qrCodeDO.setStatus(qrCode.getStatus());
        qrCodeDO.setIntent(qrCode.getIntent());
        qrCodeDO.setCreatedAt(qrCode.getCreatedAt());
        qrCodeDO.setUpdatedAt(qrCode.getUpdatedAt());
        qrCodeDO.setExpiresAt(qrCode.getExpiresAt());
        qrCodeDO.setOwnerId(qrCode.getOwnerId());
        qrCodeDO.setOwnerType(qrCode.getOwnerType());
        qrCodeDO.setSignature(qrCode.getSignature());
        return qrCodeDO;
    }
}