package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.item.SessionItem;
import com.alipay.business.core.model.domain.ReceiptItemDomain;

import java.util.List;
import java.util.Map;

public interface SessionService {
    String createSession(String receiptId, String receiptUrl, List<ReceiptSubItem> items, String userId);

    ReceiptSessionData getReceiptSession(String sessionId);

    void updateSelection(String sessionId, String userId, Map<String, Integer> itemQuantities);

    List<SessionItem> commitSelection(String sessionId, String userId);

    void updateReceiptItemStatus(String receiptId, List<ReceiptItemDomain> paidItems);
}
