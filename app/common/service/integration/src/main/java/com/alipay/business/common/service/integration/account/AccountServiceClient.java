package com.alipay.business.common.service.integration.account;

import com.alipay.alipay_plus.common.service.facade.baseresult.AccountBizResult;
import com.alipay.alipay_plus.common.service.facade.item.AccountInfoItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionRecordItem;
import com.alipay.alipay_plus.common.service.facade.request.InsertTransactionRecordRequest;
import com.alipay.alipay_plus.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.alipay_plus.common.service.facade.request.TransferRequest;
import com.alipay.alipay_plus.common.service.facade.request.UpdateTransactionRecordRequest;

public interface AccountServiceClient {

    AccountBizResult<String> transfer(TransferRequest request);

    AccountBizResult<AccountInfoItem> queryAccountInfo(QueryAccountInfoRequest request);

    AccountBizResult<String> insertTransactionRecord(InsertTransactionRecordRequest request);

    AccountBizResult<TransactionRecordItem> updateTransactionRecord(UpdateTransactionRecordRequest request);
}
