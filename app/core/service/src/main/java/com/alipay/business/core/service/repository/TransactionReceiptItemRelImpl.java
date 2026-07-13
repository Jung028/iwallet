package com.alipay.business.core.service.repository;

import com.alipay.business.common.dal.auto.custom.TransactionReceiptItemRelDAO;
import com.alipay.business.common.dal.auto.dataobject.TransactionReceiptItemRelDO;
import com.alipay.business.common.service.facade.request.QueryTransactionReceiptItemRelRequest;
import com.alipay.business.core.model.converter.DomainConverter;
import com.alipay.business.core.model.domain.TransactionReceiptItemRel;
import com.alipay.business.core.model.exception.RepositoryException;
import com.alipay.business.core.service.TransactionReceiptItemRelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class TransactionReceiptItemRelImpl implements TransactionReceiptItemRelRepository {

    @Autowired
    private TransactionReceiptItemRelDAO transactionReceiptItemRelDAO;

    @Override
    public void insertTransactionReceiptItemRel(TransactionReceiptItemRel transactionReceiptItemRel) {
        try {
            TransactionReceiptItemRelDO transactionReceiptItemRelDO = DomainConverter.convertToDO(transactionReceiptItemRel);
            int rows = transactionReceiptItemRelDAO.insertTransactionReceiptItemRel(transactionReceiptItemRelDO);
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
    public List<TransactionReceiptItemRel> queryTransactionReceiptItemRel(QueryTransactionReceiptItemRelRequest request) {
        try {
            List<TransactionReceiptItemRelDO> transactionReceiptItemRelDOs = transactionReceiptItemRelDAO.queryTransactionReceiptItemRel(request.getTxnId());
            return DomainConverter.convertToModel(transactionReceiptItemRelDOs);
        } catch (RepositoryException e) {
            throw e;
        }
    }

    @Override
    public int queryTotalPaidQuantityByReceiptItemId(UUID receiptItemId) {
        try {
            return transactionReceiptItemRelDAO.queryTotalPaidQuantityByReceiptItemId(receiptItemId);
        } catch (RepositoryException e) {
            throw e;
        }
    }

}
