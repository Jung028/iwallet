package com.alipay.business.common.service.facade.result;

import com.alipay.account_center.common.service.facade.item.TransactionHistoryItem;
import com.alipay.business.common.service.facade.baseresult.BusinessBasePageResult;

import java.util.List;

public class BusinessTransactionHistoryResult extends BusinessBasePageResult {
    private List<TransactionHistoryItem> transactions;

    public List<TransactionHistoryItem> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionHistoryItem> transactions) { this.transactions = transactions; }
}
