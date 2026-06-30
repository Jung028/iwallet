package com.alipay.business.common.service.facade.result;

import com.alipay.business.common.service.facade.baseresult.BusinessBaseResult;
import com.alipay.business.common.service.facade.item.ReceiptItem;

import java.util.List;

/**
 * @author adam
 * @date 28/6/2026 4:19 PM
 */
public class QueryReceiptsHistoryResult extends BusinessBaseResult {
    private List<ReceiptItem> receiptItems;

    public List<ReceiptItem> getReceiptItems() {
        return receiptItems;
    }

    public void setReceiptItems(List<ReceiptItem> receiptItems) {
        this.receiptItems = receiptItems;
    }
}
