package com.alipay.business.common.service.integration.agent;

/**
 * @author adam
 * @date 27/6/2026 10:18 PM
 */
public interface AgentServiceClient {
    OcrResult extractReceipt(String fileUrl, String mimeType);
}