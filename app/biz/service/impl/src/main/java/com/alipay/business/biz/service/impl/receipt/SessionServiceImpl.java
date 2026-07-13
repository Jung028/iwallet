package com.alipay.business.biz.service.impl.receipt;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.ReceiptItemStatus;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.item.SessionItem;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.core.model.domain.ReceiptItemDomain;
import com.alipay.business.core.model.util.AssertUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

    private static final String SESSION_KEY_PREFIX = "receipt:session:";
    private static final long SESSION_TTL_DAYS = 30;

    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Override
    public String createSession(String receiptId, String receiptUrl, List<ReceiptSubItem> ocrItems, String userId) {
        String sessionId = UUID.randomUUID().toString();
        String key = SESSION_KEY_PREFIX + sessionId;

        redisTemplate.opsForHash().put(key, "sessionId", sessionId);
        redisTemplate.opsForHash().put(key, "receiptId", receiptId);
        redisTemplate.opsForHash().put(key, "receiptUrl", receiptUrl);
        redisTemplate.opsForHash().put(key, "status", "OPEN");

        QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
        queryAccountInfoRequest.setUserId(userId);
        AccountBizResult<AccountInfoItem> accountInfo = accountServiceClient.queryAccountInfoByUserId(queryAccountInfoRequest);
        AssertUtil.notNull(accountInfo.getResult(), BusinessResultCode.ACCOUNT_NOT_FOUND, "session owner account not exist");

        redisTemplate.opsForHash().put(key, "sessionOwnerAccountId", accountInfo.getResult().getAccountId());
        // add reverse lookup so that we can retrieve session Id from the receipt Id
        redisTemplate.opsForValue()
                .set("receipt:session:lookup:" + receiptId,
                        sessionId,
                        SESSION_TTL_DAYS,
                        TimeUnit.DAYS);
        try {
            redisTemplate.opsForHash().put(key, "items",
                    objectMapper.writeValueAsString(toSessionItems(ocrItems)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize session items", e);
        }

        redisTemplate.expire(key, SESSION_TTL_DAYS, TimeUnit.DAYS);
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
            // set the owner id for payment.
            session.setSessionOwnerAccountId(String.valueOf(redisTemplate.opsForHash().get(key, "sessionOwnerAccountId")));
            return session;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read receipt session", e);
        }
    }

    @Override
    public void updateSelection(String sessionId, String userId, Map<String, Integer> itemQuantities) {
        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            List<SessionItem> items =
                    deserializeItems((String) redisTemplate.opsForHash().get(key, "items"));

            boolean changed = false;
            for (SessionItem item : items) {
                int desired = Math.max(0, itemQuantities.getOrDefault(item.getItemId(), 0));
                int currentMine = item.getClaims().getOrDefault(userId, 0);
                int othersClaimed = item.totalClaimed() - currentMine;
                // clamp to whatever's left after other people's claims — a unit already
                // held by someone else can't be double-claimed
                int capacity = Math.max(item.getQuantity() - othersClaimed, 0);
                int newMine = Math.min(desired, capacity);

                if (newMine == currentMine) {
                    continue;
                }
                if (newMine <= 0) {
                    item.getClaims().remove(userId);
                } else {
                    item.getClaims().put(userId, newMine);
                }
                item.setStatus(item.totalClaimed() >= item.getQuantity() ? "SELECTED" : "AVAILABLE");
                changed = true;
            }

            // update session state only if state of selection is changed
            if (changed) {
                redisTemplate.opsForHash().put(key, "items", objectMapper.writeValueAsString(items));
                redisTemplate.expire(key, SESSION_TTL_DAYS, TimeUnit.DAYS);
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
                .filter(item -> item.getClaims().getOrDefault(userId, 0) > 0)
                .collect(Collectors.toList());
    }

    @Override
    public void updateReceiptItemStatus(String receiptId, List<ReceiptItemDomain> paidItems) {
        // retrieve the session Id from receiptId
        String sessionId = redisTemplate.opsForValue().get("receipt:session:lookup:" + receiptId);
        AssertUtil.notNull(sessionId, BusinessResultCode.SYSTEM_EXCEPTION, "session not found for receiptId: " + receiptId);

        // update the receipt item status for each of the items. for this receipt id.
        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            List<SessionItem> items = deserializeItems((String) redisTemplate.opsForHash().get(key, "items"));
            // create a set to prevent duplicate, retrieve item ids
            Set<String> paidItemsIds = paidItems.stream()
                    .map(item -> item.getItemId().toString())
                    .collect(Collectors.toSet());

            // for each item, check if it contains id, then set status to PAID, remove claims (selection)
            for (SessionItem item : items) {
                if (paidItemsIds.contains(item.getItemId())) {
                    item.setStatus(ReceiptItemStatus.PAID.getCode());
                    item.getClaims().clear();
                }
            }

            // put items
            redisTemplate.opsForHash().put(key, "items", objectMapper.writeValueAsString(items));

            // set expiry
            redisTemplate.expire(key, SESSION_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<SessionItem> toSessionItems(List<ReceiptSubItem> ocrItems) {
        List<SessionItem> items = new ArrayList<>();
        if (ocrItems == null) return items;
        for (int i = 0; i < ocrItems.size(); i++) {
            ReceiptSubItem ocr = ocrItems.get(i);
            SessionItem item = new SessionItem();
            item.setItemId(ocr.getItemId().toString());
            item.setName(ocr.getName());
            item.setQuantity(ocr.getQuantity());
            item.setPrice(ocr.getTotalPrice());
            item.setTaxAmount(ocr.getTotalTaxAmount());
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
