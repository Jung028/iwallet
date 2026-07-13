package com.alipay.business.core.service;

import com.alipay.business.common.service.facade.request.QueryTransactionReceiptItemRelRequest;
import com.alipay.business.core.model.domain.TransactionReceiptItemRel;

import java.util.List;

/**
 * @author adam
 * @date 12/7/2026 7:22 PM
 */
public interface TransactionReceiptItemRelRepository {
    void insertTransactionReceiptItemRel(TransactionReceiptItemRel transactionReceiptItemRel);

    List<TransactionReceiptItemRel> queryTransactionReceiptItemRel(QueryTransactionReceiptItemRelRequest request);
}