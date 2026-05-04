package com.alipay.business.core.model.converter;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionHistoryItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.QueryTransactionHistoryResult;
import com.alipay.business.common.service.facade.item.IdempotencyKeysItem;
import com.alipay.business.common.service.facade.result.BusinessBalanceResult;
import com.alipay.business.common.service.facade.result.BusinessTransactionDetailsResult;
import com.alipay.business.core.model.domain.IdempotencyKeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * reason for this converter is to prevent account item from being exposed to user center
 */
public class ItemConverter {
    public static List<TransactionHistoryItem> convertToTxnHistory(AccountBizResult<QueryTransactionHistoryResult> result) {
        if  (result == null || result.getResult().getTransactionHistoryList().isEmpty()) {
            return Collections.emptyList();
        }
        List<TransactionHistoryItem> items = new ArrayList<>();
        //stream
        result.getResult().getTransactionHistoryList().forEach(item -> {
            TransactionHistoryItem transactionHistoryItem = new TransactionHistoryItem();
            transactionHistoryItem.setTxnId(item.getTxnId());
            transactionHistoryItem.setGmtCreate(item.getGmtCreate());
            transactionHistoryItem.setPayeeAccountId(item.getPayeeAccountId());
            transactionHistoryItem.setCompletedAt(item.getCompletedAt());
            transactionHistoryItem.setTransactionDirection(item.getTransactionDirection());
            transactionHistoryItem.setTransactionType(item.getTransactionType());
            transactionHistoryItem.setAmount(item.getAmount());
            transactionHistoryItem.setCurrency(item.getCurrency());
            transactionHistoryItem.setStatus(item.getStatus());
            transactionHistoryItem.setExtInfo(item.getExtInfo());
            items.add(transactionHistoryItem);
        });
        return items;
    }

    public static BusinessTransactionDetailsResult convertToTxnDetails(AccountBizResult<TransactionRecordItem> accountBizResult) {
        BusinessTransactionDetailsResult result = new BusinessTransactionDetailsResult();
        if (accountBizResult == null || accountBizResult.getResult() == null) {
            return result;
        }
        // TODO: add the payer name, so that we can display to the user, what is our
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
        result.setExtInfo(accountBizResult.getResult().getExtInfo());
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
        result.setAccRelationId(accountInfo.getResult().getAccountRelationId());
        result.setCurrency(accountInfo.getResult().getCurrency());
        result.setBalance(accountInfo.getResult().getBalance());
        result.setStatus(accountInfo.getResult().getStatus());
        result.setGmtCreate(accountInfo.getResult().getGmtCreate());
        result.setGmtModified(accountInfo.getResult().getGmtModified());
        result.setExtInfo(accountInfo.getResult().getExtInfo());
        return result;
    }

    public static IdempotencyKeysItem convertToIdempotencyKeys(IdempotencyKeys idempotencyKeys) {
        IdempotencyKeysItem idempotencyKeyItem = new IdempotencyKeysItem();
        idempotencyKeyItem.setUserId(idempotencyKeys.getUserId());
        idempotencyKeyItem.setReferenceId(idempotencyKeys.getReferenceId());
        idempotencyKeyItem.setStatus(idempotencyKeys.getStatus());
        idempotencyKeyItem.setRetryCount(idempotencyKeyItem.getRetryCount());
        return idempotencyKeyItem;
    }
}
