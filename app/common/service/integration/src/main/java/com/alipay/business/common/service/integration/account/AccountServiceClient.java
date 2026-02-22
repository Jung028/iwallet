package com.alipay.business.common.service.integration.account;

import com.alipay.alipay_plus.common.service.facade.baseresult.AccountBizResult;
import com.alipay.alipay_plus.common.service.facade.item.AccountInfoItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionHistoryItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionRecordItem;
import com.alipay.alipay_plus.common.service.facade.request.*;

import java.util.List;

public interface AccountServiceClient {

    AccountBizResult<String> createAccount(CreateAccountRequest request);

    AccountBizResult<String> transfer(TransferRequest request);

    AccountBizResult<AccountInfoItem> queryAccountInfo(QueryAccountInfoRequest request);

    AccountBizResult<TransactionRecordItem> queryTransactionRecord(QueryTransactionRecordRequest request);

    AccountBizResult<List<TransactionHistoryItem>> queryTransactionHistory(QueryTransactionHistoryRequest request);

    AccountBizResult<String> insertTransactionRecord(InsertTransactionRecordRequest request);

    AccountBizResult<TransactionRecordItem> updateTransactionRecord(UpdateTransactionRecordRequest request);
}
