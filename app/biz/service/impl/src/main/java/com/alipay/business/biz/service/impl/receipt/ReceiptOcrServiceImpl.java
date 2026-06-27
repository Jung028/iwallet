package com.alipay.business.biz.service.impl.receipt;

import com.alipay.business.common.service.integration.agent.AgentServiceClient;
import com.alipay.business.common.service.integration.agent.OcrResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReceiptOcrServiceImpl implements ReceiptOcrService {

    @Autowired
    private AfsStorageService afsStorageService;

    @Autowired
    private AgentServiceClient agentServiceClient;

    @Override
    public OcrResult extractReceipt(String objectKey, String mimeType) {
        String presignedGetUrl = afsStorageService.generatePresignedGetUrl(objectKey);
        return agentServiceClient.extractReceipt(presignedGetUrl, mimeType);
    }
}
