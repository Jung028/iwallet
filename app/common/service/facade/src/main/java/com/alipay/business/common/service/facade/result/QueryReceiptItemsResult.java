package com.alipay.business.common.service.facade.result;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseResult;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;

import java.util.List;

/**
 * @author adam
 * @date 30/6/2026 5:35 PM
 */
public class QueryReceiptItemsResult extends BusinessBaseResult {
    private List<ReceiptSubItem> receiptItems;

    public List<ReceiptSubItem> getReceiptItems() {
        return receiptItems;
    }

    public void setReceiptItems(List<ReceiptSubItem> receiptItems) {
        this.receiptItems = receiptItems;
    }
}