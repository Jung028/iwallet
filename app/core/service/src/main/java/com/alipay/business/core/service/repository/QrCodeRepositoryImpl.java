package com.alipay.business.core.service.repository;

import com.alipay.business.common.dal.auto.custom.QrCodeDAO;
import com.alipay.business.common.dal.auto.dataobject.QrCodeDO;
import com.alipay.business.core.model.converter.QrCodeConvertor;
import com.alipay.business.core.model.domain.QrCode;
import com.alipay.business.core.model.exception.RepositoryException;
import com.alipay.business.core.service.QrCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author adam
 * @date 24/4/2026 5:36 PM
 */
@Repository
public class QrCodeRepositoryImpl implements QrCodeRepository {

    @Autowired
    private QrCodeDAO qrCodeDAO;

    @Override
    public void insertQrCode(QrCode qrCode) {
        try {
            QrCodeDO qrCodeDO = QrCodeConvertor.convertToDO(qrCode);
            int rows = qrCodeDAO.insertQrCode(qrCodeDO);
            if (rows <= 0) {
                throw new RepositoryException("AutoReloadConfig insert failed");
            }
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateQrCode(String qrId, String signature, String status) {

    }
}