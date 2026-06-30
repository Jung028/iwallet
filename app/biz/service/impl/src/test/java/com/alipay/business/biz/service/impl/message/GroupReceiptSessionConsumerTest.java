package com.alipay.business.biz.service.impl.message;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.ReferenceType;
import com.alipay.account_center.common.service.facade.enums.TransactionCategory;
import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.account_center.common.service.facade.event.EcTransactionEvent;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.business.biz.service.impl.event.ReceiptItemPaidEvent;
import com.alipay.business.common.service.facade.enums.ReceiptItemStatus;
import com.alipay.business.common.service.facade.request.UpdateReceiptItemRequest;
import com.alipay.business.core.model.domain.ReceiptItemDomain;
import com.alipay.business.common.service.facade.request.UpdateReceiptRequest;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.service.ReceiptItemRepository;
import com.alipay.business.core.service.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupReceiptSessionConsumerTest {

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private ReceiptItemRepository receiptItemRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private GroupReceiptSessionConsumer consumer;

    @BeforeEach
    void setUp() {
        // make the transaction template execute the callback synchronously
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
    }

    @Test
    void onMessage_ignoresEventWhenCategoryIsNotGroupReceipt() {
        EcTransactionEvent event = mock(EcTransactionEvent.class);
        when(event.getTxnCategory()).thenReturn("TRANSFER");

        consumer.onMessage(event);

        verifyNoInteractions(accountServiceClient, receiptItemRepository, receiptRepository, eventPublisher);
    }

    @Test
    void onMessage_ignoresGroupReceiptEventWhenStatusIsNotFinish() {
        EcTransactionEvent event = mock(EcTransactionEvent.class);
        when(event.getTxnCategory()).thenReturn(TransactionCategory.GROUP_RECEIPT.getCode());
        when(event.getTxnStatus()).thenReturn("PENDING");

        consumer.onMessage(event);

        verifyNoInteractions(accountServiceClient, receiptItemRepository, receiptRepository, eventPublisher);
    }

    @Test
    void onMessage_processesGroupReceiptFinishedEvent_updatesReceiptItemAndReceipt() {
        EcTransactionEvent event = buildFinishedGroupReceiptEvent("TXN-001", "ACC-001");

        stubTransactionRecord("ACC-001", "TXN-001", ReferenceType.RECEIPT_ITEM.name(), "QR-001");
        stubAccountInfo("ACC-001", "John Doe");

        ReceiptItemDomain unpaidItem = buildReceiptItem(1L, 10L, ReceiptItemStatus.UNPAID, new BigDecimal("50.00"));
        when(receiptItemRepository.lockReceiptItemByQrId("QR-001")).thenReturn(unpaidItem);

        Receipt receipt = buildReceipt(10L, new BigDecimal("100.00"), new BigDecimal("0.00"));
        when(receiptRepository.queryReceiptByReceiptId(any())).thenReturn(receipt);

        ReceiptItemDomain paidItem = buildReceiptItem(1L, 10L, ReceiptItemStatus.PAID, new BigDecimal("50.00"));
        ReceiptItemDomain anotherUnpaid = buildReceiptItem(2L, 10L, ReceiptItemStatus.UNPAID, new BigDecimal("50.00"));
        when(receiptItemRepository.queryReceiptItemsByReceiptId("10")).thenReturn(Arrays.asList(paidItem, anotherUnpaid));

        consumer.onMessage(event);

        ArgumentCaptor<UpdateReceiptItemRequest> itemCaptor = ArgumentCaptor.forClass(UpdateReceiptItemRequest.class);
        verify(receiptItemRepository).updateReceiptItem(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getItemStatus()).isEqualTo(ReceiptItemStatus.PAID.name());
        assertThat(itemCaptor.getValue().getName()).isEqualTo("John Doe");
        assertThat(itemCaptor.getValue().getReceiptItemId()).isEqualTo("1");

        ArgumentCaptor<UpdateReceiptRequest> receiptCaptor = ArgumentCaptor.forClass(UpdateReceiptRequest.class);
        verify(receiptRepository).updateReceipt(receiptCaptor.capture());
        assertThat(receiptCaptor.getValue().getTotalAmountPaid()).isEqualByComparingTo("50.00");

        ArgumentCaptor<ReceiptItemPaidEvent> eventCaptor = ArgumentCaptor.forClass(ReceiptItemPaidEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getReceiptItem()).isSameAs(unpaidItem);
    }

    @Test
    void onMessage_skipsUpdateWhenReceiptItemIsAlreadyPaid_butStillPublishesEvent() {
        EcTransactionEvent event = buildFinishedGroupReceiptEvent("TXN-002", "ACC-002");

        stubTransactionRecord("ACC-002", "TXN-002", ReferenceType.RECEIPT_ITEM.name(), "QR-002");
        stubAccountInfo("ACC-002", "Jane Doe");

        ReceiptItemDomain alreadyPaid = buildReceiptItem(2L, 10L, ReceiptItemStatus.PAID, new BigDecimal("50.00"));
        when(receiptItemRepository.lockReceiptItemByQrId("QR-002")).thenReturn(alreadyPaid);

        consumer.onMessage(event);

        verify(receiptItemRepository, never()).updateReceiptItem(any());
        verify(receiptRepository, never()).queryReceiptByReceiptId(any());
        verify(receiptRepository, never()).updateReceipt(any());
        verify(eventPublisher).publishEvent(any(ReceiptItemPaidEvent.class));
    }

    @Test
    void onMessage_calculatesTotalPaid_sumsPaidItemsOnly() {
        EcTransactionEvent event = buildFinishedGroupReceiptEvent("TXN-003", "ACC-003");

        stubTransactionRecord("ACC-003", "TXN-003", ReferenceType.RECEIPT_ITEM.name(), "QR-003");
        stubAccountInfo("ACC-003", "Alice");

        ReceiptItemDomain unpaidItem = buildReceiptItem(3L, 20L, ReceiptItemStatus.UNPAID, new BigDecimal("30.00"));
        when(receiptItemRepository.lockReceiptItemByQrId("QR-003")).thenReturn(unpaidItem);

        Receipt receipt = buildReceipt(20L, new BigDecimal("100.00"), new BigDecimal("0.00"));
        when(receiptRepository.queryReceiptByReceiptId(any())).thenReturn(receipt);

        ReceiptItemDomain paid1 = buildReceiptItem(4L, 20L, ReceiptItemStatus.PAID, new BigDecimal("20.00"));
        ReceiptItemDomain paid2 = buildReceiptItem(5L, 20L, ReceiptItemStatus.PAID, new BigDecimal("10.00"));
        ReceiptItemDomain unpaid = buildReceiptItem(6L, 20L, ReceiptItemStatus.UNPAID, new BigDecimal("70.00"));
        when(receiptItemRepository.queryReceiptItemsByReceiptId("20")).thenReturn(Arrays.asList(paid1, paid2, unpaid));

        consumer.onMessage(event);

        ArgumentCaptor<UpdateReceiptRequest> captor = ArgumentCaptor.forClass(UpdateReceiptRequest.class);
        verify(receiptRepository).updateReceipt(captor.capture());
        assertThat(captor.getValue().getTotalAmountPaid()).isEqualByComparingTo("30.00");
    }

    @Test
    void onMessage_calculatesTotalPaid_ignoresNullPriceItems() {
        EcTransactionEvent event = buildFinishedGroupReceiptEvent("TXN-004", "ACC-004");

        stubTransactionRecord("ACC-004", "TXN-004", ReferenceType.RECEIPT_ITEM.name(), "QR-004");
        stubAccountInfo("ACC-004", "Bob");

        ReceiptItemDomain unpaidItem = buildReceiptItem(7L, 30L, ReceiptItemStatus.UNPAID, new BigDecimal("25.00"));
        when(receiptItemRepository.lockReceiptItemByQrId("QR-004")).thenReturn(unpaidItem);

        Receipt receipt = buildReceipt(30L, new BigDecimal("50.00"), new BigDecimal("0.00"));
        when(receiptRepository.queryReceiptByReceiptId(any())).thenReturn(receipt);

        ReceiptItemDomain paidWithNull = buildReceiptItem(8L, 30L, ReceiptItemStatus.PAID, null);
        ReceiptItemDomain paidWithPrice = buildReceiptItem(9L, 30L, ReceiptItemStatus.PAID, new BigDecimal("25.00"));
        when(receiptItemRepository.queryReceiptItemsByReceiptId("30")).thenReturn(Arrays.asList(paidWithNull, paidWithPrice));

        consumer.onMessage(event);

        ArgumentCaptor<UpdateReceiptRequest> captor = ArgumentCaptor.forClass(UpdateReceiptRequest.class);
        verify(receiptRepository).updateReceipt(captor.capture());
        assertThat(captor.getValue().getTotalAmountPaid()).isEqualByComparingTo("25.00");
    }

    // --- helpers ---

    private EcTransactionEvent buildFinishedGroupReceiptEvent(String txnId, String payerAccountNo) {
        EcTransactionEvent event = mock(EcTransactionEvent.class);
        when(event.getTxnCategory()).thenReturn(TransactionCategory.GROUP_RECEIPT.getCode());
        when(event.getTxnStatus()).thenReturn(TransactionStatusEnum.FINISH.getCode());
        when(event.getTxnId()).thenReturn(txnId);
        when(event.getPayerAccountNo()).thenReturn(payerAccountNo);
        return event;
    }

    @SuppressWarnings("unchecked")
    private void stubTransactionRecord(String payerAccountNo, String txnId, String referenceType, String referenceId) {
        TransactionRecordItem record = mock(TransactionRecordItem.class);
        when(record.getReferenceType()).thenReturn(referenceType);
        when(record.getReferenceId()).thenReturn(referenceId);
        AccountBizResult<TransactionRecordItem> result = mock(AccountBizResult.class);
        when(result.getResult()).thenReturn(record);
        when(accountServiceClient.queryTransactionRecord(any())).thenReturn(result);
    }

    @SuppressWarnings("unchecked")
    private void stubAccountInfo(String accountId, String accountName) {
        AccountInfoItem info = mock(AccountInfoItem.class);
        when(info.getAccountName()).thenReturn(accountName);
        AccountBizResult<AccountInfoItem> result = mock(AccountBizResult.class);
        when(result.getResult()).thenReturn(info);
        when(accountServiceClient.queryAccountInfo(any())).thenReturn(result);
    }

    private ReceiptItemDomain buildReceiptItem(Long itemId, Long receiptId, ReceiptItemStatus status, BigDecimal totalPrice) {
        ReceiptItemDomain item = new ReceiptItemDomain();
        item.setItemId(itemId);
        item.setReceiptId(receiptId);
        item.setStatus(status.getCode());
        item.setTotalPrice(totalPrice);
        return item;
    }

    private Receipt buildReceipt(Long receiptId, BigDecimal totalAmount, BigDecimal totalAmountPaid) {
        Receipt receipt = new Receipt();
        receipt.setReceiptId(receiptId);
        receipt.setTotalAmount(totalAmount);
        receipt.setTotalAmountPaid(totalAmountPaid);
        return receipt;
    }
}
