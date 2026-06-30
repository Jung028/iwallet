package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.integration.agent.OcrResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SessionServiceImpl implements SessionService {

    private static final String SESSION_KEY_PREFIX = "receipt:session:";
    private static final long SESSION_TTL_MINUTES = 30;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String createSession(String receiptId, String receiptUrl, List<OcrResult.OcrLineItem> ocrItems) {
        String sessionId = UUID.randomUUID().toString();

        ReceiptSessionData session = new ReceiptSessionData();
        session.setSessionId(sessionId);
        session.setReceiptId(receiptId);
        session.setReceiptUrl(receiptUrl);
        session.setStatus("OPEN");
        session.setItems(toSessionItems(ocrItems));

        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, json, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create receipt session", e);
        }

        return sessionId;
    }

    @Override
    public ReceiptSessionData getReceiptSession(String sessionId) {
        String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
        if (json == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        try {
            return objectMapper.readValue(json, ReceiptSessionData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read receipt session", e);
        }
    }

    @Override
    public void updateSelection(String sessionId, String userId, List<String> itemIds) {
        ReceiptSessionData session = getReceiptSession(sessionId);

        for (ReceiptSessionData.SessionItem item : session.getItems()) {
            if (itemIds.contains(item.getItemId())) {
                item.setSelectedBy(userId);
                item.setStatus("SELECTED");
            } else if (userId.equals(item.getSelectedBy())) {
                item.setSelectedBy(null);
                item.setStatus("AVAILABLE");
            }
        }

        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, json, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update receipt session", e);
        }
    }

    private List<ReceiptSessionData.SessionItem> toSessionItems(List<OcrResult.OcrLineItem> ocrItems) {
        List<ReceiptSessionData.SessionItem> items = new ArrayList<>();
        if (ocrItems == null) return items;
        for (int i = 0; i < ocrItems.size(); i++) {
            OcrResult.OcrLineItem ocr = ocrItems.get(i);
            ReceiptSessionData.SessionItem item = new ReceiptSessionData.SessionItem();
            item.setItemId(String.valueOf(i + 1));
            item.setName(ocr.getName());
            item.setPrice(ocr.getUnitPrice().multiply(BigDecimal.valueOf(ocr.getQuantity())));
            item.setStatus("AVAILABLE");
            items.add(item);
        }
        return items;
    }
}
