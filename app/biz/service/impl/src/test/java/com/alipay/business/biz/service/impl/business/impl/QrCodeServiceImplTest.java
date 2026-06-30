package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.business.biz.service.impl.template.BusinessServiceTemplate;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.request.QueryReceiptsHistoryRequest;
import com.alipay.business.common.service.facade.result.QueryReceiptsHistoryResult;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.service.ReceiptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceImplTest {

    // use real template so the abstract callback methods are invoked naturally
    @Spy
    private BusinessServiceTemplate businessServiceTemplate = new BusinessServiceTemplate();

    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private QrCodeServiceImpl qrCodeService;

    @Test
    void queryReceiptsHistory_returnsConvertedReceiptItems() {
        Receipt receipt1 = buildReceipt(1L, "receipt-1.pdf", new BigDecimal("100.00"), new BigDecimal("60.00"), "ACTIVE");
        Receipt receipt2 = buildReceipt(2L, "receipt-2.jpg", new BigDecimal("200.00"), new BigDecimal("200.00"), "SETTLED");
        when(receiptRepository.queryReceiptsHistory(any())).thenReturn(Arrays.asList(receipt1, receipt2));

        QueryReceiptsHistoryRequest request = new QueryReceiptsHistoryRequest();
        request.setUserId("user-123");

        BusinessBizResult<QueryReceiptsHistoryResult> response = qrCodeService.queryReceiptsHistory(request);

        assertThat(response).isNotNull();
        QueryReceiptsHistoryResult result = response.getResult();
        assertThat(result).isNotNull();

        List<ReceiptItem> items = result.getReceiptItems();
        assertThat(items).hasSize(2);

        ReceiptItem item1 = items.get(0);
        assertThat(item1.getReceiptId()).isEqualTo(1L);
        assertThat(item1.getReceiptName()).isEqualTo("receipt-1.pdf");
        assertThat(item1.getStatus()).isEqualTo("ACTIVE");
        assertThat(item1.getTotalPaid()).isEqualByComparingTo("60.00");
        assertThat(item1.getTotalUnpaid()).isEqualByComparingTo("40.00");

        ReceiptItem item2 = items.get(1);
        assertThat(item2.getReceiptId()).isEqualTo(2L);
        assertThat(item2.getTotalPaid()).isEqualByComparingTo("200.00");
        assertThat(item2.getTotalUnpaid()).isEqualByComparingTo("0.00");
    }

    @Test
    void queryReceiptsHistory_returnsEmptyListWhenNoReceipts() {
        when(receiptRepository.queryReceiptsHistory(any())).thenReturn(Collections.emptyList());

        QueryReceiptsHistoryRequest request = new QueryReceiptsHistoryRequest();
        request.setUserId("user-456");

        BusinessBizResult<QueryReceiptsHistoryResult> response = qrCodeService.queryReceiptsHistory(request);

        assertThat(response).isNotNull();
        assertThat(response.getResult().getReceiptItems()).isEmpty();
    }

    @Test
    void queryReceiptsHistory_passesRequestToRepository() {
        when(receiptRepository.queryReceiptsHistory(any())).thenReturn(Collections.emptyList());

        QueryReceiptsHistoryRequest request = new QueryReceiptsHistoryRequest();
        request.setUserId("user-789");
        request.setReceiptId("receipt-abc");

        qrCodeService.queryReceiptsHistory(request);

        verify(receiptRepository).queryReceiptsHistory(same(request));
    }

    // --- helpers ---

    private Receipt buildReceipt(Long receiptId, String fileName, BigDecimal totalAmount,
                                  BigDecimal totalAmountPaid, String status) {
        Receipt receipt = new Receipt();
        receipt.setReceiptId(receiptId);
        receipt.setFileName(fileName);
        receipt.setTotalAmount(totalAmount);
        receipt.setTotalAmountPaid(totalAmountPaid);
        receipt.setStatus(status);
        receipt.setCreatedAt(new Date());
        return receipt;
    }
}
