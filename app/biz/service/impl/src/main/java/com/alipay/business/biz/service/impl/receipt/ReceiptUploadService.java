package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.request.ConfirmUploadRequest;

public interface ReceiptUploadService {

    UploadUrlResponse generatePresignedUrl(String userId);

    ReceiptFileMetadata validateAndPersist(ConfirmUploadRequest request, String userId);
}
