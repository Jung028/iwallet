package com.alipay.business.common.service.integration.account;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionHistoryItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AccountServiceClient {

    AccountBizResult<String> createAccount(CreateAccountRequest createAccountRequest);

    AccountBizResult<AccountInfoItem> queryAccountInfo(QueryAccountInfoRequest request);

    AccountBizResult<TransactionRecordItem> queryTransactionRecord(QueryTransactionRecordRequest request);

    AccountBizResult<QueryTransactionHistoryResult> queryTransactionHistory(QueryTransactionHistoryRequest request);

    AccountBizResult<TransactionRecordItem> insertTransactionRecord(InsertTransactionRecordRequest request);

    AccountBizResult<TransactionRecordItem> updateTransactionRecord(UpdateTransactionRecordRequest request);

    AccountBizResult<TransactionRecordItem> queryTransactionByStatus(QueryTransactionRecordRequest transactionRecordRequest);

    AccountBizResult<AccountInfoItem> queryAccountInfoByUserId(QueryAccountInfoRequest queryAccountInfoRequest);
}
