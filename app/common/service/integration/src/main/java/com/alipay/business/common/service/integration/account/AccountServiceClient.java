package com.alipay.business.common.service.integration.account;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.*;
import org.springframework.stereotype.Service;

@Service
public interface AccountServiceClient {

    AccountBizResult<AccountInfoItem> queryAccountInfo(QueryAccountInfoRequest request);

    AccountBizResult<TransactionRecordItem> queryTransactionRecord(QueryTransactionRecordRequest request);

    AccountBizResult<QueryTransactionHistoryResult> queryTransactionHistory(QueryTransactionHistoryRequest request);

    AccountBizResult<TransactionRecordItem> insertTransactionRecord(InsertTransactionRecordRequest request);

    void updateTransactionRecord(UpdateTransactionRecordRequest request);

    AccountBizResult<TransactionRecordItem> queryTransactionByStatus(QueryTransactionRecordRequest transactionRecordRequest);

    AccountBizResult<AccountInfoItem> queryAccountInfoByUserId(QueryAccountInfoRequest queryAccountInfoRequest);
}
