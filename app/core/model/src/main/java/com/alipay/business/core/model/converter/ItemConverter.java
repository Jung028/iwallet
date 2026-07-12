package com.alipay.business.core.model.converter;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionHistoryItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.QueryTransactionHistoryResult;
import com.alipay.business.common.service.facade.item.IdempotencyKeysItem;
import com.alipay.business.common.service.facade.item.QrCodeItem;
import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.result.BusinessBalanceResult;
import com.alipay.business.common.service.facade.result.BusinessTransactionDetailsResult;
import com.alipay.business.common.service.facade.result.QueryQrCodesResult;
import com.alipay.business.common.service.facade.result.QueryReceiptItemsResult;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.domain.QrCode;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.domain.ReceiptItemDomain;

import java.math.BigDecimal;
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

    public static List<ReceiptItem> convertToReceipt(List<Receipt> receipts) {
        List<ReceiptItem> receiptItems = new ArrayList<>();
        for (Receipt receipt : receipts) {
            ReceiptItem receiptItem = new ReceiptItem();
            receiptItem.setReceiptId(receipt.getReceiptId());
            receiptItem.setStatus(receipt.getStatus() != null ? receipt.getStatus() : null);
            BigDecimal totalAmount = receipt.getTotalAmount() != null ? receipt.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal totalPaid = receipt.getTotalAmountPaid() != null ? receipt.getTotalAmountPaid() : BigDecimal.ZERO;
            receiptItem.setTotalAmountPaid(totalPaid);
            receiptItem.setTotalAmountUnpaid(totalAmount.subtract(totalPaid));
            receiptItem.setTotalTaxAmount(receipt.getTotalTaxAmount());
            receiptItem.setCreatedAt(receipt.getCreatedAt());
            receiptItem.setFileName(receipt.getFileName());
            receiptItem.setReferenceId(receipt.getReferenceId());
            receiptItems.add(receiptItem);
        }
        return receiptItems;
    }

    public static QueryReceiptItemsResult convertToReceiptItem(List<ReceiptItemDomain> domains) {
        List<ReceiptSubItem> items = new ArrayList<>();
        for (ReceiptItemDomain domain : domains) {
            ReceiptSubItem item = new ReceiptSubItem();
            item.setItemId(domain.getItemId());
            item.setReceiptId(domain.getReceiptId());
            item.setName(domain.getName());
            item.setQuantity(domain.getQuantity());
            item.setUnitPrice(domain.getUnitPrice());
            item.setTotalPrice(domain.getTotalPrice());
            item.setSelectedBy(domain.getSelectedBy());
            item.setStatus(domain.getStatus());
            item.setQrReferenceId(domain.getQrReferenceId());
            item.setCreatedAt(domain.getCreatedAt());
            item.setUpdatedAt(domain.getUpdatedAt());
            item.setTotalTaxAmount(domain.getTotalTaxAmount());
            items.add(item);
        }
        QueryReceiptItemsResult result = new QueryReceiptItemsResult();
        result.setReceiptItems(items);
        return result;
    }

    public static QueryQrCodesResult convertToQrCodes(List<QrCode> qrCodes, int total) {
        List<QrCodeItem> items = new ArrayList<>();
        for (QrCode qrCode : qrCodes) {
            QrCodeItem item = new QrCodeItem();
            item.setQrId(qrCode.getQrId());
            item.setOwnerId(qrCode.getOwnerId());
            item.setOwnerType(qrCode.getOwnerType());
            item.setIntent(qrCode.getIntent());
            item.setAmount(qrCode.getAmount());
            item.setCurrency(qrCode.getCurrency());
            item.setStatus(qrCode.getStatus());
            item.setExpiresAt(qrCode.getExpiresAt());
            item.setCreatedAt(qrCode.getCreatedAt());
            items.add(item);
        }
        QueryQrCodesResult result = new QueryQrCodesResult();
        result.setQrCodes(items);
        result.setTotalCount(total);
        return result;
    }
}
