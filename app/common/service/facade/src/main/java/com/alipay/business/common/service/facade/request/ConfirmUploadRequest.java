package com.alipay.business.common.service.facade.request;

public class ConfirmUploadRequest {
    private String objectKey;
    private String originalFileName;

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
}
