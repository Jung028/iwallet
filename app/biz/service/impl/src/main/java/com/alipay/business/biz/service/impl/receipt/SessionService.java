package com.alipay.business.biz.service.impl.receipt;

import java.util.List;

public interface SessionService {
    String createSession(String receiptId, String receiptUrl, List<OcrResult.OcrLineItem> items);
    ReceiptSessionData getReceiptSession(String sessionId);
    void updateSelection(String sessionId, String userId, List<String> itemIds);
}
