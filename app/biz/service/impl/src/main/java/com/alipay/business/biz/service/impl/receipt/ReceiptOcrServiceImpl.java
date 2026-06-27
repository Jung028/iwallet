package com.alipay.business.biz.service.impl.receipt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReceiptOcrServiceImpl implements ReceiptOcrService {

    @Autowired
    private AfsStorageService afsStorageService;

    @Autowired
    private IAgentClient iAgentClient;

    @Override
    public OcrResult extractReceipt(String objectKey) {
        String presignedGetUrl = afsStorageService.generatePresignedGetUrl(objectKey);
        return iAgentClient.extractReceipt(presignedGetUrl);
    }
}
