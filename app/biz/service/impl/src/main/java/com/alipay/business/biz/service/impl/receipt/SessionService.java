package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.item.SessionItem;

import java.util.List;

public interface SessionService {
    String createSession(String receiptId, String receiptUrl, List<ReceiptSubItem> items);
    ReceiptSessionData getReceiptSession(String sessionId);
    void updateSelection(String sessionId, String userId, List<String> itemIds);
    List<SessionItem> commitSelection(String sessionId, String userId);
}
