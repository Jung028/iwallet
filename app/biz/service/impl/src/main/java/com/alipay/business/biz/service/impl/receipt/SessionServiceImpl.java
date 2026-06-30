package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.item.SessionItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

    private static final String SESSION_KEY_PREFIX = "receipt:session:";
    private static final long SESSION_TTL_MINUTES = 30;

    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Override
    public String createSession(String receiptId, String receiptUrl, List<ReceiptSubItem> ocrItems) {
        String sessionId = UUID.randomUUID().toString();
        String key = SESSION_KEY_PREFIX + sessionId;

        redisTemplate.opsForHash().put(key, "sessionId", sessionId);
        redisTemplate.opsForHash().put(key, "receiptId", receiptId);
        redisTemplate.opsForHash().put(key, "receiptUrl", receiptUrl);
        redisTemplate.opsForHash().put(key, "status", "OPEN");

        try {
            redisTemplate.opsForHash().put(key, "items",
                    objectMapper.writeValueAsString(toSessionItems(ocrItems)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize session items", e);
        }

        redisTemplate.expire(key, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        return sessionId;
    }

    @Override
    public ReceiptSessionData getReceiptSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String receiptId = (String) redisTemplate.opsForHash().get(key, "receiptId");
        if (receiptId == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        try {
            ReceiptSessionData session = new ReceiptSessionData();
            session.setSessionId(sessionId);
            session.setReceiptId(receiptId);
            session.setReceiptUrl(String.valueOf(redisTemplate.opsForHash().get(key, "receiptUrl")));
            session.setStatus(String.valueOf(redisTemplate.opsForHash().get(key, "status")));
            session.setItems(deserializeItems(String.valueOf(redisTemplate.opsForHash().get(key, "items"))));
            return session;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read receipt session", e);
        }
    }

    @Override
    public void updateSelection(String sessionId, String userId, List<String> itemIds) {
        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            List<SessionItem> items =
                    deserializeItems((String) redisTemplate.opsForHash().get(key, "items"));

            boolean changed = false;
            for (SessionItem item : items) {
                String lockKey = "session:" + sessionId + ":item:" + item.getItemId();
                if (itemIds.contains(item.getItemId())) {

                    // add a lock key for idempotency, prevents multiple requests causing race condition
                    Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                            lockKey, userId, 10, TimeUnit.MINUTES
                    );
                    if (Boolean.TRUE.equals(locked)) {
                        // select
                        item.setSelectedBy(userId);
                        item.setStatus("SELECTED");
                        changed = true;
                    }
                } else if (userId.equals(item.getSelectedBy())) {
                    // remember to delete the lockKey when user unselect it.
                    redisTemplate.opsForHash().delete(lockKey, userId);
                    // unselect
                    item.setSelectedBy(null);
                    item.setStatus("AVAILABLE");
                    changed = true;
                }
            }

            // update session state only if state of selection is changed
            if (changed) {
                redisTemplate.opsForHash().put(key, "items", objectMapper.writeValueAsString(items));
                redisTemplate.expire(key, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
                //send WS event
                ReceiptSessionData updated = getReceiptSession(sessionId);
                messagingTemplate.convertAndSend("/topic/receipt/" + sessionId, updated);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update session", e);
        }
    }

    @Override
    public List<SessionItem> commitSelection(String sessionId, String userId) {
        ReceiptSessionData session = getReceiptSession(sessionId);
        return session.getItems().stream()
                .filter(item -> userId.equals(item.getSelectedBy()))
                .collect(Collectors.toList());
    }

    private List<SessionItem> toSessionItems(List<ReceiptSubItem> ocrItems) {
        List<SessionItem> items = new ArrayList<>();
        if (ocrItems == null) return items;
        for (int i = 0; i < ocrItems.size(); i++) {
            ReceiptSubItem ocr = ocrItems.get(i);
            SessionItem item = new SessionItem();
            item.setItemId(String.valueOf(i + 1));
            item.setName(ocr.getName());
            item.setPrice(ocr.getUnitPrice().multiply(BigDecimal.valueOf(ocr.getQuantity())));
            item.setStatus("AVAILABLE");
            items.add(item);
        }
        return items;
    }

    private List<SessionItem> deserializeItems(String json) throws Exception {
        if (json == null) return new ArrayList<>();
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, SessionItem.class));
    }
}
