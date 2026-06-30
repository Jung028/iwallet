package com.alipay.business.core.model.converter;

import com.alipay.business.common.dal.auto.dataobject.ReceiptDO;
import com.alipay.business.common.dal.auto.dataobject.ReceiptItemDO;
import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSession;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.domain.ReceiptItemDomain;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author adam
 * @date 28/6/2026 4:49 PM
 */
public class DomainConverter {

    public static List<Receipt> convertToModelList(List<ReceiptDO> receiptsDO) {
        if (CollectionUtils.isEmpty(receiptsDO)) {
            return Collections.emptyList();
        }
        return receiptsDO.stream()
                .map(DomainConverter::convertToModel)
                .collect(Collectors.toList());
    }

    public static Receipt convertToModel(ReceiptDO receiptDO) {
        if (receiptDO == null) {
            return null;
        }
        Receipt receipt = new Receipt();
        receipt.setReceiptId(receiptDO.getReceiptId());
        receipt.setUserId(receiptDO.getUserId());
        receipt.setObjectKey(receiptDO.getObjectKey());
        receipt.setBucketName(receiptDO.getBucketName());
        receipt.setFileUrl(receiptDO.getFileUrl());
        receipt.setFileName(receiptDO.getFileName());
        receipt.setContentType(receiptDO.getContentType());
        receipt.setFileSize(receiptDO.getFileSize());
        receipt.setStatus(receiptDO.getStatus());
        receipt.setTotalAmount(receiptDO.getTotalAmount());
        receipt.setTotalAmountPaid(receiptDO.getTotalAmountPaid());
        receipt.setCreatedAt(receiptDO.getCreatedAt());
        receipt.setUpdatedAt(receiptDO.getUpdatedAt());
        receipt.setReferenceId(receiptDO.getReferenceId());
        return receipt;
    }

    public static ReceiptItemDO convertToDO(ReceiptSubItem receiptSubItem) {
        if (receiptSubItem == null) {
            return null;
        }
        ReceiptItemDO receiptItemDO = new ReceiptItemDO();
        receiptItemDO.setItemId(receiptSubItem.getItemId());
        receiptItemDO.setReceiptId(receiptSubItem.getReceiptId());
        receiptItemDO.setName(receiptSubItem.getName());
        receiptItemDO.setQuantity(receiptSubItem.getQuantity());
        receiptItemDO.setUnitPrice(receiptSubItem.getUnitPrice());
        receiptItemDO.setTotalPrice(receiptSubItem.getTotalPrice());
        receiptItemDO.setSelectedBy(receiptSubItem.getSelectedBy());
        receiptItemDO.setStatus(receiptSubItem.getStatus());
        receiptItemDO.setQrReferenceId(receiptSubItem.getQrReferenceId());
        receiptItemDO.setCreatedAt(receiptSubItem.getCreatedAt());
        receiptItemDO.setUpdatedAt(receiptSubItem.getUpdatedAt());
        return receiptItemDO;
    }

    public static ReceiptItemDomain convertToModel(ReceiptItemDO receiptItemDO) {
        if (receiptItemDO == null) {
            return null;
        }
        ReceiptItemDomain domain = new ReceiptItemDomain();
        domain.setItemId(receiptItemDO.getItemId());
        domain.setReceiptId(receiptItemDO.getReceiptId());
        domain.setName(receiptItemDO.getName());
        domain.setQuantity(receiptItemDO.getQuantity());
        domain.setUnitPrice(receiptItemDO.getUnitPrice());
        domain.setTotalPrice(receiptItemDO.getTotalPrice());
        domain.setSelectedBy(receiptItemDO.getSelectedBy());
        domain.setStatus(receiptItemDO.getStatus());
        domain.setQrReferenceId(receiptItemDO.getQrReferenceId());
        domain.setCreatedAt(receiptItemDO.getCreatedAt());
        domain.setUpdatedAt(receiptItemDO.getUpdatedAt());
        return domain;
    }
}
