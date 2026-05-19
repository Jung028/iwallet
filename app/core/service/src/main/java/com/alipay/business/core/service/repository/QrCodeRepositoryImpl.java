package com.alipay.business.core.service.repository;

import com.alipay.business.common.dal.auto.custom.QrCodeDAO;
import com.alipay.business.common.dal.auto.dataobject.QrCodeDO;
import com.alipay.business.common.service.facade.item.QrCodeItem;
import com.alipay.business.common.service.facade.request.QueryQrCodesRequest;
import com.alipay.business.common.service.facade.request.ToggleQrRequest;
import com.alipay.business.common.service.facade.result.QueryQrCodesResult;
import com.alipay.business.core.model.converter.QrCodeConvertor;
import com.alipay.business.core.model.domain.QrCode;
import com.alipay.business.core.model.exception.RepositoryException;
import com.alipay.business.core.service.QrCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

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
    public void updateQrCode(String qrId, String status) {

    }

    @Override
    public void toggleQrCode(ToggleQrRequest request) {
        try {
            int rows = qrCodeDAO.toggleQrCode(request.getQrId(), request.isToggleQr());
            if (rows <= 0) {
                throw new RepositoryException("AutoReloadConfig toggle failed");
            }
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public QueryQrCodesResult queryQrCodes(QueryQrCodesRequest request) {
        List<QrCodeDO> doList = qrCodeDAO.queryQrCodes(
                request.getMerchantId(), request.getPageSize(), request.getOffset());
        int total = qrCodeDAO.countQrCodes(request.getMerchantId());

        List<QrCodeItem> items = doList.stream().map(do_ -> {
            QrCodeItem item = new QrCodeItem();
            item.setQrId(do_.getQrId());
            item.setOwnerId(do_.getOwnerId());
            item.setOwnerType(do_.getOwnerType());
            item.setIntent(do_.getIntent());
            item.setAmount(do_.getAmount());
            item.setCurrency(do_.getCurrency());
            item.setStatus(do_.getStatus());
            item.setExpiresAt(do_.getExpiresAt());
            item.setCreatedAt(do_.getCreatedAt());
            return item;
        }).collect(Collectors.toList());

        QueryQrCodesResult result = new QueryQrCodesResult();
        result.setQrCodes(items);
        result.setTotalCount(total);
        return result;
    }
}