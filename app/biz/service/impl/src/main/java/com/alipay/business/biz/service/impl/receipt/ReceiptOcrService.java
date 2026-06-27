package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.integration.agent.OcrResult;

public interface ReceiptOcrService {
    OcrResult extractReceipt(String objectKey, String mimeType);
}
