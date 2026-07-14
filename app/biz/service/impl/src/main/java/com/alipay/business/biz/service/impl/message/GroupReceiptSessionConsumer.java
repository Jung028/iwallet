package com.alipay.business.biz.service.impl.message;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.ReferenceType;
import com.alipay.account_center.common.service.facade.enums.TransactionCategory;
import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.account_center.common.service.facade.event.EcTransactionEvent;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.account_center.common.service.facade.request.QueryTransactionRecordRequest;
import com.alipay.business.biz.service.impl.event.ReceiptItemPaidEvent;
import com.alipay.business.biz.service.impl.receipt.SelectItemsRequest;
import com.alipay.business.biz.service.impl.receipt.SessionService;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.ReceiptItemStatus;
import com.alipay.business.common.service.facade.request.QueryReceiptRequest;
import com.alipay.business.common.service.facade.request.QueryTransactionReceiptItemRelRequest;
import com.alipay.business.common.service.facade.request.UpdateReceiptItemRequest;
import com.alipay.business.common.service.facade.request.UpdateReceiptRequest;
import com.alipay.business.common.service.integration.account.AccountServiceClient;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.domain.ReceiptItemDomain;
import com.alipay.business.core.model.domain.TransactionReceiptItemRel;
import com.alipay.business.core.model.enums.ReceiptStatus;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.business.core.service.ReceiptItemRepository;
import com.alipay.business.core.service.ReceiptRepository;
import com.alipay.business.core.service.TransactionReceiptItemRelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author adam
 * @date 28/6/2026 11:42 PM
 */
@Service
public class GroupReceiptSessionConsumer {

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Autowired
    private ReceiptItemRepository receiptItemRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TransactionReceiptItemRelRepository transactionReceiptItemRelRepository;

    @KafkaListener(topics = "EC_TRANSACTION_RESULT", groupId = "business-center-group-receipt")
    public void onMessage(EcTransactionEvent event) {
        // first, check that the category is GROUP_RECEIPT and update because every single group receipt transaction
        // needs to update total paid amount
        // ensure that we only process the transactions that are finished and is a group receipt.
        System.out.println("TXN_CATEGORY" + event.getTxnCategory());
        if (TransactionCategory.GROUP_RECEIPT.getCode().equals(event.getTxnCategory()) &&
                TransactionStatusEnum.FINISH.getCode().equals(event.getTxnStatus())) {
            QueryTransactionRecordRequest queryTransactionRecordRequest = new QueryTransactionRecordRequest();
            queryTransactionRecordRequest.setTxnId(event.getTxnId());
            queryTransactionRecordRequest.setAccountId(event.getPayerAccountNo());
            AccountBizResult<TransactionRecordItem> transactionRecord = accountServiceClient.queryTransactionRecord(queryTransactionRecordRequest);

            // verify that its receipt item, so we dont process other qr transactions
            AssertUtil.isTrue(transactionRecord.getResult().getReferenceType().equals(ReferenceType.RECEIPT_ITEM.name()),
                    BusinessResultCode.SYSTEM_EXCEPTION, "reference type must be RECEIPT_ITEM if txn category is GROUP_RECEIPT");

            // get the name of payer
            QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
            queryAccountInfoRequest.setAccountId(event.getPayerAccountNo());
            AccountBizResult<AccountInfoItem> accountInfoItem =
                    accountServiceClient.queryAccountInfo(queryAccountInfoRequest);
            String payerName = accountInfoItem.getResult().getAccountName();

            // there is an issue. what if theere are multiple items selected? we need to put these items somewhere. or select them somewhere.
            // rollback if any exception thrown
            ReceiptItemPaidEvent receiptItemPaidEvent =
                    transactionTemplate.execute(status -> {
                        // handle multiple items. so first we query transaction_receipt_item_rel by transaction id,
                        QueryTransactionReceiptItemRelRequest queryTransactionReceiptItemRelRequest = new QueryTransactionReceiptItemRelRequest();
                        queryTransactionReceiptItemRelRequest.setTxnId(event.getTxnId());
                        List<TransactionReceiptItemRel> transactionReceiptItemRelList = transactionReceiptItemRelRepository.
                                queryTransactionReceiptItemRel(queryTransactionReceiptItemRelRequest);

                        String receiptId = null;
                        // for each receipt item,
                        List<ReceiptItemDomain> paidItems = new ArrayList<>();

                        for (TransactionReceiptItemRel receiptItemRel : transactionReceiptItemRelList) {

                            ReceiptItemDomain lockedReceiptItem =
                                    receiptItemRepository.lockReceiptItemByItemId(
                                            receiptItemRel.getReceiptItemId().toString()
                                    );

                            AssertUtil.notNull(lockedReceiptItem, BusinessResultCode.SYSTEM_EXCEPTION,
                                    "receipt item not found for transaction");

                            if (receiptId == null) {
                                receiptId = lockedReceiptItem.getReceiptId().toString();
                            }

                            Integer transactionQuantity = receiptItemRel.getReceiptItemQuantity();
                            System.out.println("TRANSACTION_QUANTITY: " + transactionQuantity);

                            System.out.println("RECEIPT_ITEM_ID" +  receiptItemRel.getReceiptItemId());
                            int totalPaidQuantity = transactionReceiptItemRelRepository
                                            .queryTotalPaidQuantityByReceiptItemId(receiptItemRel.getReceiptItemId());

                            // means there is still same item quantity not paid for yet
                            if (totalPaidQuantity >= lockedReceiptItem.getQuantity()) {
                                if (!ReceiptItemStatus.PAID.getCode().equals(lockedReceiptItem.getStatus())) {
                                    UpdateReceiptItemRequest updateRequest = new UpdateReceiptItemRequest();
                                    updateRequest.setReceiptItemId(lockedReceiptItem.getItemId().toString());
                                    updateRequest.setItemStatus(ReceiptItemStatus.PAID.name());
                                    updateRequest.setName(payerName);
                                    updateRequest.setGmtUpdatedAt(new Date());
                                    receiptItemRepository.updateReceiptItem(updateRequest);
                                }
                            }

                            ReceiptItemDomain paidItem = new ReceiptItemDomain();
                            paidItem.setItemId(lockedReceiptItem.getItemId());
                            paidItem.setReceiptId(lockedReceiptItem.getReceiptId());
                            paidItem.setName(lockedReceiptItem.getName());
                            paidItem.setQuantity(transactionQuantity);
                            paidItem.setStatus(ReceiptItemStatus.PAID.getCode());
                            paidItems.add(paidItem);
                        }
                        // retrieve the receipt, update the status.
                        AssertUtil.notNull(receiptId, BusinessResultCode.SYSTEM_EXCEPTION, "receipt item not found for transaction");
                        QueryReceiptRequest request = new QueryReceiptRequest();
                        request.setReceiptId(receiptId);
                        Receipt receipt = receiptRepository.queryReceiptByReceiptId(request);
                        BigDecimal totalPaid = calculateTotalPaid(receipt);

                        // update the total amount paid
                        UpdateReceiptRequest updateReceiptRequest = new UpdateReceiptRequest();
                        // update to completed if the total paid is equal to the total amount of the receipt
                        System.out.println("TOTAL_AMOUNT" + receipt.getTotalAmount() + ", TOTAL_PAID" + totalPaid);
                        if (totalPaid.equals(receipt.getTotalAmount())) {
                            updateReceiptRequest.setStatus(ReceiptStatus.COMPLETED.getCode());
                        }
                        updateReceiptRequest.setReceiptId(receiptId);
                        updateReceiptRequest.setTotalAmountPaid(totalPaid);
                        receiptRepository.updateReceipt(updateReceiptRequest);

                        return new ReceiptItemPaidEvent(
                                receiptId,
                                paidItems
                        );
            });

            // update the cache result as well
            sessionService.updateReceiptItemStatus(receiptItemPaidEvent.getReceiptId(), receiptItemPaidEvent.getReceiptItems());
        }

    }

    /**
     * calculate total paid
     * @param receipt
     * @return
     */
    private BigDecimal calculateTotalPaid(Receipt receipt) {

        List<ReceiptItemDomain> allItems =
                receiptItemRepository.queryReceiptItemsByReceiptId(
                        receipt.getReceiptId().toString()
                );

        return allItems.stream()
                .filter(item ->
                        ReceiptItemStatus.PAID.getCode()
                                .equals(item.getStatus()))
                .map(item ->
                        item.getTotalPrice()
                                .add(Optional.ofNullable(item.getTotalTaxAmount())
                                        .orElse(BigDecimal.ZERO))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
