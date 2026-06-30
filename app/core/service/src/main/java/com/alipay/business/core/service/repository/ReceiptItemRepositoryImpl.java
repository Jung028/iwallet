package com.alipay.business.core.service.repository;

import com.alipay.business.common.dal.auto.custom.ReceiptItemDAO;
import com.alipay.business.common.dal.auto.dataobject.ReceiptItemDO;
import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.request.UpdateReceiptItemRequest;
import com.alipay.business.core.model.converter.DomainConverter;
import com.alipay.business.core.model.domain.ReceiptItemDomain;
import com.alipay.business.core.model.exception.RepositoryException;
import com.alipay.business.core.service.ReceiptItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ReceiptItemRepositoryImpl implements ReceiptItemRepository {

    @Autowired
    private ReceiptItemDAO receiptItemDAO;

    @Override
    public void insertReceiptItem(ReceiptSubItem receiptSubItem) {
        try {
            ReceiptItemDO receiptItemDO = DomainConverter.convertToDO(receiptSubItem);
            int rows = receiptItemDAO.insertItemReceipt(receiptItemDO);
            if (rows <= 0) {
                throw new RepositoryException("no rows updated");
            }
        } catch (RepositoryException e) {
            throw e;
        }
    }

    @Override
    public ReceiptItemDomain lockReceiptItemByQrId(String referenceId) {
        ReceiptItemDO receiptItemDO = receiptItemDAO.lockReceiptItemByQrId(referenceId);
        return DomainConverter.convertToModel(receiptItemDO);
    }

    @Override
    public void updateReceiptItem(UpdateReceiptItemRequest req) {
        int rows = receiptItemDAO.updateReceiptItem(
                req.getReceiptItemId(),
                req.getItemStatus(),
                req.getName(),   // selectedBy = payer name
                req.getGmtUpdatedAt()
        );
        if (rows <= 0) {
            throw new RepositoryException("updateReceiptItem: no rows affected for id " + req.getReceiptItemId());
        }
    }

    @Override
    public List<ReceiptItemDomain> queryReceiptItemsByReceiptId(String receiptId) {
        List<ReceiptItemDO> items = receiptItemDAO.queryReceiptItemsByReceiptId(receiptId);
        return items.stream()
                .map(DomainConverter::convertToModel)
                .collect(Collectors.toList());
    }

}
