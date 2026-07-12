package com.alipay.business.core.model.converter;

import com.alipay.business.common.dal.auto.dataobject.ReceiptDO;
import com.alipay.business.core.model.domain.Receipt;

public class ReceiptConvertor {

    public static ReceiptDO covertToDO(Receipt receipt) {
        ReceiptDO receiptDO = new ReceiptDO();
        receiptDO.setReceiptId(receipt.getReceiptId());
        receiptDO.setCreatedAt(receipt.getCreatedAt());
        receiptDO.setUpdatedAt(receipt.getUpdatedAt());
        receiptDO.setBucketName(receipt.getBucketName());
        receiptDO.setFileName(receipt.getFileName());
        receiptDO.setFileSize(receipt.getFileSize());
        receiptDO.setStatus(receipt.getStatus());
        receiptDO.setObjectKey(receipt.getObjectKey());
        receiptDO.setFileUrl(receipt.getFileUrl());
        receiptDO.setTotalAmount(receipt.getTotalAmount());
        receiptDO.setUserId(receipt.getUserId());
        receiptDO.setReferenceId(receipt.getReferenceId());
        receiptDO.setTotalAmountPaid(receipt.getTotalAmountPaid());
        receiptDO.setTotalTaxAmount(receipt.getTotalTaxAmount());
        return receiptDO;
    }
}
