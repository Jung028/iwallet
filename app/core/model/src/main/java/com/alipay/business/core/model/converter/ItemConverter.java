package com.alipay.business.core.model.converter;

import com.alipay.alipay_plus.common.service.facade.baseresult.AccountBizResult;
import com.alipay.alipay_plus.common.service.facade.item.AccountInfoItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionHistoryItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionRecordItem;
import com.alipay.business.common.service.facade.result.BusinessBalanceResult;
import com.alipay.business.common.service.facade.result.BusinessTransactionDetailsResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemConverter {
    public static List<TransactionHistoryItem> convertToTxnHistory(AccountBizResult<List<TransactionHistoryItem>> result) {
        if  (result == null || result.getResult().isEmpty()) {
            return Collections.emptyList();
        }
        List<TransactionHistoryItem> items = new ArrayList<>();
        //stream
        result.getResult().forEach(item -> {
            TransactionHistoryItem transactionHistoryItem = new TransactionHistoryItem();
            transactionHistoryItem.setAmount(item.getAmount());
            transactionHistoryItem.setStatus(item.getStatus());
            transactionHistoryItem.setCurrency(item.getCurrency());
            transactionHistoryItem.setDesc(item.getDesc());
            transactionHistoryItem.setTxnId(item.getTxnId());
            transactionHistoryItem.setDirection(item.getDirection());
            transactionHistoryItem.setGmtCreate(item.getGmtCreate());
            items.add(transactionHistoryItem);
        });
        return items;
    }

    public static BusinessTransactionDetailsResult convertToTxnDetails(AccountBizResult<TransactionRecordItem> accountBizResult) {
        BusinessTransactionDetailsResult result = new BusinessTransactionDetailsResult();
        if (accountBizResult == null || accountBizResult.getResult() == null) {
            return result;
        }
        result.setTxnId(accountBizResult.getResult().getTxnId());
        result.setGmtCreate(accountBizResult.getResult().getGmtCreate());
        result.setGmtModified(accountBizResult.getResult().getGmtModified());
        result.setGmtComplete(accountBizResult.getResult().getGmtComplete());
        result.setPayerAccountId(accountBizResult.getResult().getPayerAccountId());
        result.setPayeeAccountId(accountBizResult.getResult().getPayeeAccountId());
        result.setAmount(accountBizResult.getResult().getAmount());
        result.setCurrency(accountBizResult.getResult().getCurrency());
        result.setTxnType(accountBizResult.getResult().getTxnType());
        result.setTxnStatus(accountBizResult.getResult().getTxnStatus());
        result.setFailureReason(accountBizResult.getResult().getFailureReason());
        result.setDesc(accountBizResult.getResult().getDesc());
        return result;
    }

    public static BusinessBalanceResult convertToBalanceResult(AccountBizResult<AccountInfoItem> accountInfo) {
        BusinessBalanceResult result = new BusinessBalanceResult();
        if (accountInfo == null || accountInfo.getResult() == null) {
            return result;
        }
        result.setAccountId(accountInfo.getResult().getAccountId());
        result.setAccountNumber(accountInfo.getResult().getAccountNumber());
        result.setAccountName(accountInfo.getResult().getAccountName());
        result.setAccountType(accountInfo.getResult().getAccountType());
        result.setAccRelationId(accountInfo.getResult().getAccRelationId());
        result.setCurrency(accountInfo.getResult().getCurrency());
        result.setBalance(accountInfo.getResult().getBalance());
        result.setStatus(accountInfo.getResult().getStatus());
        result.setGmtCreate(accountInfo.getResult().getGmtCreate());
        result.setGmtModified(accountInfo.getResult().getGmtModified());
        result.setDesc(accountInfo.getResult().getDesc());
        return result;
    }
}
