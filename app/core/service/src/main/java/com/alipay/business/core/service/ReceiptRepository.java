package com.alipay.business.core.service;

import com.alipay.business.common.service.facade.request.QueryReceiptsHistoryRequest;
import com.alipay.business.common.service.facade.request.QueryReceiptRequest;
import com.alipay.business.common.service.facade.request.UpdateReceiptRequest;
import com.alipay.business.core.model.domain.Receipt;

import java.util.List;

public interface ReceiptRepository {

    void insertReceipt(Receipt receipt);

    List<Receipt> queryReceiptsHistory(QueryReceiptsHistoryRequest request);

    Receipt queryReceiptByReceiptId(QueryReceiptRequest queryReceiptRequest);

    void updateReceipt(UpdateReceiptRequest updateReceiptRequest);
}
