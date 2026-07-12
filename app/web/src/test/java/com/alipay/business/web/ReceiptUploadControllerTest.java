package com.alipay.business.web;

import com.alipay.business.biz.service.impl.auth.JwtClaims;
import com.alipay.business.biz.service.impl.auth.JwtContextHolder;
import com.alipay.business.biz.service.impl.receipt.ReceiptSessionData;
import com.alipay.business.biz.service.impl.receipt.SessionService;
import com.alipay.business.common.service.facade.api.QrCodeService;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.item.SessionItem;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.core.service.ReceiptItemRepository;
import com.alipay.business.core.service.ReceiptRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises the commit-selection write path with mocked repositories so a missed field mapping
 * (e.g. unitPrice never set, caught live as a Postgres NOT NULL violation) fails fast here
 * instead of surfacing only when a real payment is committed against a live database.
 */
@ExtendWith(MockitoExtension.class)
class ReceiptUploadControllerTest {

    @Mock SessionService sessionService;
    @Mock QrCodeService qrCodeService;
    @Mock ReceiptRepository receiptRepository;
    @Mock ReceiptItemRepository receiptItemRepository;
    @InjectMocks ReceiptUploadController controller;

    private static final String USER_ID = "user-1";
    private static final String SESSION_ID = "sess-1";
    private static final String RECEIPT_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        JwtClaims claims = new JwtClaims();
        claims.setSubject(USER_ID);
        JwtContextHolder.set(claims);
    }

    @AfterEach
    void tearDown() {
        JwtContextHolder.clear();
    }

    @Test
    void commitSession_neverInsertsNullRequiredColumns() {
        SessionItem item = new SessionItem();
        item.setItemId("1");
        item.setName("Fried Chicken");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("20.00"));
        item.setTaxAmount(new BigDecimal("1.20"));
        item.getClaims().put(USER_ID, 1); // claimed 1 of the 2 units

        ReceiptSessionData session = new ReceiptSessionData();
        session.setSessionId(SESSION_ID);
        session.setReceiptId(RECEIPT_ID);
        session.setItems(List.of(item));
        when(sessionService.getReceiptSession(SESSION_ID)).thenReturn(session);

        BusinessBizResult<String> qrResult = new BusinessBizResult<>();
        qrResult.setResult("qr-token");
        when(qrCodeService.generateQrCode(any(GenerateQrCodeRequest.class))).thenReturn(qrResult);

        controller.commitSession(SESSION_ID);

        ArgumentCaptor<ReceiptSubItem> captor = ArgumentCaptor.forClass(ReceiptSubItem.class);
        verify(receiptItemRepository).insertReceiptItem(captor.capture());
        ReceiptSubItem inserted = captor.getValue();

        // receipt_item table NOT NULL columns
        assertThat(inserted.getItemId()).isNotNull();
        assertThat(inserted.getReceiptId()).isNotNull();
        assertThat(inserted.getName()).isNotNull();
        assertThat(inserted.getQuantity()).isNotNull();
        assertThat(inserted.getUnitPrice()).isNotNull();
        assertThat(inserted.getTotalPrice()).isNotNull();
        assertThat(inserted.getStatus()).isNotNull();
        assertThat(inserted.getCreatedAt()).isNotNull();
        assertThat(inserted.getUpdatedAt()).isNotNull();

        // claimed 1 of 2 units of a $20 line => half the price and tax
        assertThat(inserted.getUnitPrice()).isEqualByComparingTo("10.00");
        assertThat(inserted.getTotalPrice()).isEqualByComparingTo("10.00");
        assertThat(inserted.getTotalTaxAmount()).isEqualByComparingTo("0.60");
    }

    @Test
    void commitSession_throwsWhenNothingSelected() {
        SessionItem item = new SessionItem();
        item.setItemId("1");
        item.setName("Fried Chicken");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("20.00"));
        item.setTaxAmount(new BigDecimal("1.20"));
        // no claims for USER_ID

        ReceiptSessionData session = new ReceiptSessionData();
        session.setSessionId(SESSION_ID);
        session.setReceiptId(RECEIPT_ID);
        session.setItems(List.of(item));
        when(sessionService.getReceiptSession(SESSION_ID)).thenReturn(session);

        try {
            controller.commitSession(SESSION_ID);
            assertThat(false).as("expected IllegalStateException").isTrue();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains("No items selected");
        }
    }
}
