package com.alipay.business.core.service.repository;

import com.alipay.business.common.dal.auto.custom.ReceiptDAO;
import com.alipay.business.common.dal.auto.dataobject.ReceiptDO;
import com.alipay.business.core.model.converter.ReceiptConvertor;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.exception.RepositoryException;
import com.alipay.business.core.service.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ReceiptRepositoryImpl implements ReceiptRepository {

    @Autowired
    private ReceiptDAO receiptDAO;

    @Override
    public void insertReceipt(Receipt receipt) {
        try {
            ReceiptDO receiptDO = ReceiptConvertor.covertToDO(receipt);
            int rows = receiptDAO.insertReceipt(receiptDO);
            if (rows < 0) {
                throw new RepositoryException("Failed to insert receipt, no rows affected");
            }
        } catch (RepositoryException e) {
            throw e;
        }
    }
}
