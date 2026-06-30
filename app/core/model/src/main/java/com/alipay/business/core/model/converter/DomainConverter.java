package com.alipay.business.core.model.converter;

import com.alipay.business.common.dal.auto.dataobject.ReceiptDO;
import com.alipay.business.common.dal.auto.dataobject.ReceiptItemDO;
import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSession;
import com.alipay.business.core.model.domain.Receipt;
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
        receipt.setCreatedAt(receiptDO.getCreatedAt());
        receipt.setUpdatedAt(receiptDO.getUpdatedAt());
        receipt.setReferenceId(receipt.getReferenceId());
        return receipt;
    }

    public static ReceiptItemDO convertToDO(ReceiptItem receiptItem) {
        if (receiptItem == null) {
            return null;
        }
        ReceiptItemDO receiptItemDO = new ReceiptItemDO();
        receiptItemDO.setItemId(receiptItem.getItemId());
        receiptItemDO.setReceiptId(receiptItem.getReceiptId());
        receiptItemDO.setName(receiptItem.getName());
        receiptItemDO.setQuantity(receiptItem.getQuantity());
        receiptItemDO.setUnitPrice(receiptItem.getUnitPrice());
        receiptItemDO.setTotalPrice(receiptItem.getTotalPrice());
        receiptItemDO.setSelectedBy(receiptItem.getSelectedBy());
        receiptItemDO.setStatus(receiptItem.getStatus());
        receiptItemDO.setQrReferenceId(receiptItem.getQrReferenceId());
        receiptItemDO.setCreatedAt(receiptItem.getCreatedAt());
        receiptItemDO.setUpdatedAt(receiptItem.getUpdatedAt());
        return receiptItemDO;
    }

    public static ReceiptItem convertToModel(ReceiptItemDO receiptItemDO) {
        if (receiptItemDO == null) {
            return null;
        }
        ReceiptItem receiptItem = new ReceiptItem();
        receiptItem.setItemId(receiptItemDO.getItemId());
        receiptItem.setReceiptId(receiptItemDO.getReceiptId());
        receiptItem.setName(receiptItemDO.getName());
        receiptItem.setQuantity(receiptItemDO.getQuantity());
        receiptItem.setUnitPrice(receiptItemDO.getUnitPrice());
        receiptItem.setTotalPrice(receiptItemDO.getTotalPrice());
        receiptItem.setSelectedBy(receiptItemDO.getSelectedBy());
        receiptItem.setStatus(receiptItemDO.getStatus());
        receiptItem.setQrReferenceId(receiptItemDO.getQrReferenceId());
        receiptItem.setCreatedAt(receiptItemDO.getCreatedAt());
        receiptItem.setUpdatedAt(receiptItemDO.getUpdatedAt());
        return receiptItem;
    }
}
