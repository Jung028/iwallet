package com.alipay.business.common.service.integration.agent;

import java.util.HashMap;
import java.util.Map;

public class ExtractReceiptRequest {
    private String sourceDocumentId;
    private String fileUrl;
    private String mimeType;
    private Map<String, Object> metadata = new HashMap<>();

    public String getSourceDocumentId() { return sourceDocumentId; }
    public void setSourceDocumentId(String sourceDocumentId) { this.sourceDocumentId = sourceDocumentId; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
