package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.request.ConfirmUploadRequest;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.enums.ReceiptStatus;
import com.alipay.business.core.service.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class ReceipServiceImpl implements ReceiptService {

    @Autowired
    private AfsStorageService afsStorageService;

    @Autowired
    private ReceiptRepository receiptRepository;

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

        OcrResult ocrResult = receiptOcrService.extractReceipt(request.getObjectKey());

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

        receiptRepository.insertReceipt(receipt);

        ReceiptUploadResult result = new ReceiptUploadResult();
        result.setReceiptId(receiptId.toString());
        result.setReceiptUrl(receiptUrl);
        result.setItems(ocrResult.getItems());
        return result;
    }
}
