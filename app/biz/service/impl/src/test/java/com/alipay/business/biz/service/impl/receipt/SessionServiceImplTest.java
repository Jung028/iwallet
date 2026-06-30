package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.item.SessionItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionServiceImplTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock HashOperations<String, Object, Object> hashOps;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Spy ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks SessionServiceImpl service;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void createSession_storesHashFieldsAndReturnsSessionId() {
        ReceiptItem item = new ReceiptItem();
        item.setName("Burger");
        item.setUnitPrice(new BigDecimal("12.50"));
        item.setQuantity(1);

        String sessionId = service.createSession("receipt-1", "https://url", List.of(item));

        assertThat(sessionId).isNotBlank();
        verify(hashOps).put(contains(sessionId), eq("receiptId"), eq("receipt-1"));
        verify(hashOps).put(contains(sessionId), eq("receiptUrl"), eq("https://url"));
        verify(hashOps).put(contains(sessionId), eq("status"), eq("OPEN"));
        verify(hashOps).put(contains(sessionId), eq("sessionId"), eq(sessionId));
        verify(hashOps).put(contains(sessionId), eq("items"), anyString());
        verify(redisTemplate).expire(contains(sessionId), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getReceiptSession_throwsWhenSessionMissing() {
        when(hashOps.get(anyString(), eq("receiptId"))).thenReturn(null);
        assertThatThrownBy(() -> service.getReceiptSession("bad-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Session not found");
    }

    @Test
    void getReceiptSession_returnsPopulatedSession() throws Exception {
        String itemsJson = mapper.writeValueAsString(List.of(Map.of(
                "itemId", "1", "name", "Burger", "price", 12.5,
                "status", "AVAILABLE")));
        when(hashOps.get(anyString(), eq("receiptId"))).thenReturn("receipt-1");
        when(hashOps.get(anyString(), eq("receiptUrl"))).thenReturn("https://url");
        when(hashOps.get(anyString(), eq("status"))).thenReturn("OPEN");
        when(hashOps.get(anyString(), eq("sessionId"))).thenReturn("sess-1");
        when(hashOps.get(anyString(), eq("items"))).thenReturn(itemsJson);

        ReceiptSessionData result = service.getReceiptSession("sess-1");

        assertThat(result.getReceiptId()).isEqualTo("receipt-1");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void updateSelection_marksItemsAndBroadcasts() throws Exception {
        SessionItem item1 = new SessionItem();
        item1.setItemId("1"); item1.setStatus("AVAILABLE"); item1.setName("Burger");
        item1.setPrice(new BigDecimal("10.00"));

        SessionItem item2 = new SessionItem();
        item2.setItemId("2"); item2.setStatus("AVAILABLE"); item2.setName("Fries");
        item2.setPrice(new BigDecimal("5.00"));

        String itemsJson = mapper.writeValueAsString(List.of(item1, item2));
        when(hashOps.get(anyString(), eq("receiptId"))).thenReturn("receipt-1");
        when(hashOps.get(anyString(), eq("receiptUrl"))).thenReturn("https://url");
        when(hashOps.get(anyString(), eq("status"))).thenReturn("OPEN");
        when(hashOps.get(anyString(), eq("sessionId"))).thenReturn("sess-1");
        when(hashOps.get(anyString(), eq("items"))).thenReturn(itemsJson);

        service.updateSelection("sess-1", "user-A", List.of("1"));

        ArgumentCaptor<String> savedJson = ArgumentCaptor.forClass(String.class);
        verify(hashOps).put(contains("sess-1"), eq("items"), savedJson.capture());

        List<?> saved = mapper.readValue(savedJson.getValue(), List.class);
        assertThat(saved).hasSize(2);

        verify(messagingTemplate).convertAndSend(eq("/topic/receipt/sess-1"), any(ReceiptSessionData.class));
    }

    @Test
    void commitSelection_returnsUserItems() throws Exception {
        SessionItem mine = new SessionItem();
        mine.setItemId("1"); mine.setSelectedBy("user-A"); mine.setStatus("SELECTED");
        mine.setName("Burger"); mine.setPrice(new BigDecimal("10.00"));

        SessionItem other = new SessionItem();
        other.setItemId("2"); other.setSelectedBy("user-B"); other.setStatus("SELECTED");
        other.setName("Fries"); other.setPrice(new BigDecimal("5.00"));

        String itemsJson = mapper.writeValueAsString(List.of(mine, other));
        when(hashOps.get(anyString(), eq("receiptId"))).thenReturn("receipt-1");
        when(hashOps.get(anyString(), eq("receiptUrl"))).thenReturn("https://url");
        when(hashOps.get(anyString(), eq("status"))).thenReturn("OPEN");
        when(hashOps.get(anyString(), eq("sessionId"))).thenReturn("sess-1");
        when(hashOps.get(anyString(), eq("items"))).thenReturn(itemsJson);

        List<SessionItem> result = service.commitSelection("sess-1", "user-A");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemId()).isEqualTo("1");
    }
}
