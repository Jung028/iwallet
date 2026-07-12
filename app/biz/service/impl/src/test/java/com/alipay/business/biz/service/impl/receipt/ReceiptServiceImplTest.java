package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.request.ConfirmUploadRequest;
import com.alipay.business.common.service.integration.agent.OcrResult;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.service.ReceiptItemRepository;
import com.alipay.business.core.service.ReceiptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Exercises the receipt-persistence write path with mocked repositories so schema mismatches
 * (missing/null NOT NULL columns) surface as a fast assertion failure instead of a live
 * PSQLException discovered by clicking through the app.
 */
@ExtendWith(MockitoExtension.class)
class ReceiptServiceImplTest {

    @Mock AfsStorageService afsStorageService;
    @Mock ReceiptOcrService receiptOcrService;
    @Mock ReceiptRepository receiptRepository;
    @Mock ReceiptItemRepository receiptItemRepository;
    @InjectMocks ReceiptServiceImpl service;

    private static OcrResult.OcrLineItem lineItem(String name, int quantity, String unitPrice, String totalPrice) {
        OcrResult.OcrLineItem item = new OcrResult.OcrLineItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        item.setTotalPrice(new BigDecimal(totalPrice));
        return item;
    }

    @Test
    void validateAndPersist_neverInsertsNullRequiredColumns() {
        AfsObjectMetadata metadata = new AfsObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength("12345");
        when(afsStorageService.headObject("receipts/user-1/a.jpg")).thenReturn(metadata);
        when(afsStorageService.getObjectUrl("receipts/user-1/a.jpg"))
                .thenReturn("http://minio/bucket/receipts/user-1/a.jpg");

        OcrResult ocrResult = new OcrResult();
        ocrResult.setTotalAmount(new BigDecimal("20.00"));
        ocrResult.setCurrency("MYR");
        ocrResult.setTaxAmount(new BigDecimal("2.00"));
        ocrResult.setSstAmount(BigDecimal.ZERO);
        ocrResult.setItems(List.of(
                lineItem("Burger", 1, "10.00", "10.00"),
                lineItem("Fries", 2, "5.00", "10.00")
        ));
        when(receiptOcrService.extractReceipt(anyString(), anyString())).thenReturn(ocrResult);

        ConfirmUploadRequest request = new ConfirmUploadRequest();
        request.setObjectKey("receipts/user-1/a.jpg");
        request.setOriginalFileName("receipt.jpg");

        ReceiptUploadResult result = service.validateAndPersist(request, "user-1");

        ArgumentCaptor<Receipt> receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).insertReceipt(receiptCaptor.capture());
        Receipt receipt = receiptCaptor.getValue();
        // receipt table NOT NULL columns
        assertThat(receipt.getReceiptId()).isNotNull();
        assertThat(receipt.getUserId()).isNotNull();
        assertThat(receipt.getObjectKey()).isNotNull();
        assertThat(receipt.getBucketName()).isNotNull();
        assertThat(receipt.getStatus()).isNotNull();
        assertThat(receipt.getCreatedAt()).isNotNull();
        assertThat(receipt.getUpdatedAt()).isNotNull();

        ArgumentCaptor<ReceiptSubItem> itemCaptor = ArgumentCaptor.forClass(ReceiptSubItem.class);
        verify(receiptItemRepository, times(2)).insertReceiptItem(itemCaptor.capture());
        for (ReceiptSubItem item : itemCaptor.getAllValues()) {
            // receipt_item table NOT NULL columns
            assertThat(item.getItemId()).isNotNull();
            assertThat(item.getReceiptId()).isNotNull();
            assertThat(item.getName()).isNotNull();
            assertThat(item.getQuantity()).isNotNull();
            assertThat(item.getUnitPrice()).isNotNull();
            assertThat(item.getTotalPrice()).isNotNull();
            assertThat(item.getStatus()).isNotNull();
            assertThat(item.getCreatedAt()).isNotNull();
            assertThat(item.getUpdatedAt()).isNotNull();
        }

        assertThat(result.getReceiptId()).isNotNull();
        assertThat(result.getItems()).hasSize(2);
    }

    @Test
    void validateAndPersist_throwsWhenFileMissingFromStorage() {
        when(afsStorageService.headObject("missing.jpg")).thenReturn(null);
        ConfirmUploadRequest request = new ConfirmUploadRequest();
        request.setObjectKey("missing.jpg");

        try {
            service.validateAndPersist(request, "user-1");
            assertThat(false).as("expected IllegalArgumentException").isTrue();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("not found");
        }
    }
}
