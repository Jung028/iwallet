package com.alipay.business.core.service;

import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.request.UpdateReceiptItemRequest;

import java.util.List;

public interface ReceiptItemRepository {

    void insertReceiptItem(ReceiptItem receiptItem);

    ReceiptItem lockReceiptItemByQrId(String referenceId);

    void updateReceiptItem(UpdateReceiptItemRequest updateReceiptItemRequest);

    List<ReceiptItem> queryReceiptItemsByReceiptId(String receiptId);

}
