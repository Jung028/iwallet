package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.request.ConfirmUploadRequest;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.enums.ReceiptStatus;
import com.alipay.business.core.service.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Service
public class ReceiptUploadServiceImpl implements ReceiptUploadService {

    @Autowired
    private AfsStorageService afsStorageService;

    @Autowired
    private ReceiptRepository receiptRepository;

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
    public ReceiptFileMetadata validateAndPersist(ConfirmUploadRequest request, String userId) {
        AfsObjectMetadata objectMetadata = afsStorageService.headObject(request.getObjectKey());
        if (objectMetadata == null) {
            throw new IllegalArgumentException("Receipt file not found");
        }

        UUID receiptId = UUID.randomUUID();

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
        receipt.setTotalAmount(BigDecimal.valueOf(0));
        receipt.setFileUrl("");
        receipt.setUpdatedAt(new Date());

        receiptRepository.insertReceipt(receipt);

        ReceiptFileMetadata metadata = new ReceiptFileMetadata();
        metadata.setReceiptId(receiptId.toString());
        metadata.setObjectKey(receipt.getObjectKey());
        metadata.setBucketName("receipt-bucket");
        metadata.setReceiptUrl(afsStorageService.getObjectUrl(receipt.getObjectKey()));
        metadata.setOriginalFileName(receipt.getFileName());
        metadata.setFileSize(String.valueOf(receipt.getFileSize()));
        metadata.setStatus(ReceiptStatus.UPLOADED.getCode());
        metadata.setCreatedTime(receipt.getCreatedAt());
        return metadata;
    }
}
