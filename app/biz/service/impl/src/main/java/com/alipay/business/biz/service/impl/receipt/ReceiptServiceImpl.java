package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.enums.ReceiptItemStatus;
import com.alipay.business.common.service.facade.item.ReceiptItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.request.ConfirmUploadRequest;
import com.alipay.business.common.service.integration.agent.OcrResult;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.enums.ReceiptStatus;
import com.alipay.business.core.service.ReceiptItemRepository;
import com.alipay.business.core.service.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alipay.business.common.service.facade.result.UploadUrlResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    @Autowired
    private AfsStorageService afsStorageService;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ReceiptItemRepository receiptItemRepository;

    @Autowired
    private ReceiptOcrService receiptOcrService;

    @Override
    public UploadUrlResponse generatePresignedUrl(String userId) {
        String objectKey = "receipts/" + userId + "/" + UUID.randomUUID() + ".jpg";
        String presignedUrl = afsStorageService.generatePresignedPutUrl(objectKey);

        UploadUrlResponse response = new UploadUrlResponse();
        response.setObjectKey(objectKey);
        response.setPresignedUrl(presignedUrl);
        response.setExpiresIn(300);
        response.setHttpMethod("PUT");
        return response;
    }

    @Override
    public ReceiptUploadResult validateAndPersist(ConfirmUploadRequest request, String userId) {
        AfsObjectMetadata objectMetadata = afsStorageService.headObject(request.getObjectKey());
        if (objectMetadata == null) {
            throw new IllegalArgumentException("Receipt file not found in storage");
        }

        OcrResult ocrResult = receiptOcrService.extractReceipt(request.getObjectKey(), objectMetadata.getContentType());

        UUID receiptId = UUID.randomUUID();
        String receiptUrl = afsStorageService.getObjectUrl(request.getObjectKey());

        Receipt receipt = new Receipt();
        receipt.setReceiptId(receiptId);
        receipt.setUserId(userId);
        receipt.setObjectKey(request.getObjectKey());
        receipt.setBucketName("receipt-bucket");
        receipt.setFileName(request.getOriginalFileName());
        receipt.setContentType(objectMetadata.getContentType());
        receipt.setFileSize(Long.valueOf(objectMetadata.getContentLength()));
        receipt.setStatus(ReceiptStatus.UPLOADED.name());
        receipt.setCreatedAt(new Date());
        receipt.setTotalAmount(ocrResult.getTotalAmount());
        receipt.setFileUrl(receiptUrl);
        receipt.setUpdatedAt(new Date());
        receipt.setTotalTaxAmount(ocrResult.getTotalTaxAmount());
        System.out.println("RECEIPT : " + receipt.getReceiptId());
        receiptRepository.insertReceipt(receipt);

        // compute subtotal from OCR items
        BigDecimal subtotal = ocrResult.getItems().stream()
                .map(OcrResult.OcrLineItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // total tax (sum of every tax/charge line extracted from the receipt)
        BigDecimal totalTax = ocrResult.getTotalTaxAmount();

        // derive tax rate
        BigDecimal taxRate = BigDecimal.ZERO;

        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            taxRate = totalTax.divide(subtotal, 8, RoundingMode.HALF_UP);
        }

        List<ReceiptSubItem> lineItems = new ArrayList<>();
        if (ocrResult.getItems() != null) {
            for (OcrResult.OcrLineItem ocr : ocrResult.getItems()) {
                ReceiptSubItem li = new ReceiptSubItem();
                li.setItemId(UUID.randomUUID());
                li.setName(ocr.getName());
                li.setQuantity(ocr.getQuantity());
                li.setUnitPrice(ocr.getUnitPrice());
                li.setTotalPrice(ocr.getTotalPrice());
                //total tax amount is price of the item * total tax,
                BigDecimal itemTax = ocr.getTotalPrice()
                        .multiply(taxRate)
                        .setScale(2, RoundingMode.HALF_UP);

                li.setTotalTaxAmount(itemTax);
                li.setStatus(ReceiptItemStatus.UNPAID.getCode());
                li.setCreatedAt(new Date());
                li.setUpdatedAt(new Date());
                li.setReceiptId(receiptId);
                System.out.println("ITEM ID : " + li.getItemId());
                receiptItemRepository.insertReceiptItem(li);
                lineItems.add(li);
            }
        }
        ReceiptUploadResult result = new ReceiptUploadResult();
        result.setReceiptId(receiptId.toString());
        result.setReceiptUrl(receiptUrl);
        result.setItems(lineItems);
        return result;
    }
}
