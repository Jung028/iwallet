package com.alipay.business.core.service;

import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.request.UpdateReceiptItemRequest;
import com.alipay.business.core.model.domain.ReceiptItemDomain;

import java.util.List;

public interface ReceiptItemRepository {

    void insertReceiptItem(ReceiptSubItem receiptSubItem);

    void updateReceiptItem(UpdateReceiptItemRequest updateReceiptItemRequest);

    List<ReceiptItemDomain> queryReceiptItemsByReceiptId(String receiptId);

    ReceiptItemDomain lockReceiptItemByItemId(String itemId);
}
