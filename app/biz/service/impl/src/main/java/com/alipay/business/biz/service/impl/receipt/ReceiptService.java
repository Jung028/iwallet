package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.facade.request.ConfirmUploadRequest;
import com.alipay.business.common.service.facade.result.UploadUrlResponse;

public interface ReceiptService {

    UploadUrlResponse generatePresignedUrl(String userId);

    ReceiptUploadResult validateAndPersist(ConfirmUploadRequest request, String userId);
}
