package com.alipay.business.core.service.repository;

import com.alipay.business.common.dal.auto.custom.ReceiptDAO;
import com.alipay.business.common.dal.auto.dataobject.ReceiptDO;
import com.alipay.business.common.service.facade.request.QueryReceiptRequest;
import com.alipay.business.common.service.facade.request.QueryReceiptsHistoryRequest;
import com.alipay.business.common.service.facade.request.UpdateReceiptRequest;
import com.alipay.business.core.model.converter.DomainConverter;
import com.alipay.business.core.model.converter.ReceiptConvertor;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.exception.RepositoryException;
import com.alipay.business.core.service.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReceiptRepositoryImpl implements ReceiptRepository {

    @Autowired
    private ReceiptDAO receiptDAO;

    @Override
    public void insertReceipt(Receipt receipt) {
        try {
            ReceiptDO receiptDO = ReceiptConvertor.covertToDO(receipt);
            int rows = receiptDAO.insertReceipt(receiptDO);
            if (rows <= 0) {
                throw new RepositoryException("Failed to insert receipt, no rows affected");
            }
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("Failed to insert receipt", e);
        }
    }

    @Override
    public List<Receipt> queryReceiptsHistory(QueryReceiptsHistoryRequest request) {
        try {
            List<ReceiptDO> receiptsDO = receiptDAO.queryReceiptsHistory(
                    request.getUserId(),
                    request.getReceiptId(),
                    request.getPageSize(),
                    request.getPageNo());
            return DomainConverter.convertToModelList(receiptsDO);
        } catch (Exception e) {
            throw new RepositoryException("Failed to query receipts history", e);
        }
    }

    @Override
    public Receipt queryReceiptByReceiptId(QueryReceiptRequest queryReceiptRequest) {
        try {
            ReceiptDO receiptDO = receiptDAO.queryReceiptByReceiptId(queryReceiptRequest.getReceiptId());
            return DomainConverter.convertToModel(receiptDO);
        } catch (RepositoryException e) {
            throw e;
        }
    }

    @Override
    public void updateReceipt(UpdateReceiptRequest updateReceiptRequest) {
        int rows = receiptDAO.updateReceipt(updateReceiptRequest.getReceiptId(),
                updateReceiptRequest.getTotalAmountPaid());
        if (rows <= 0) {
            throw new RepositoryException("updateReceipt: no rows affected for id " + updateReceiptRequest.getReceiptId());
        }
    }

    @Override
    public void updateReceiptReferenceId(String qrToken) {
    }
}
