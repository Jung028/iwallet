package com.alipay.business.biz.service.impl.receipt;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class AfsStorageService {

    private static final Logger log = LoggerFactory.getLogger(AfsStorageService.class);

    @Value("${afs.oss.endpoint}")
    private String endpoint;

    @Value("${afs.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${afs.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${afs.oss.bucketName}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            log.warn("MinIO credentials not configured — AfsStorageService running in stub mode. Set OSS_ACCESS_KEY_ID and OSS_ACCESS_KEY_SECRET.");
            return;
        }
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKeyId, accessKeySecret)
                .build();
        log.info("MinIO client initialized — endpoint: {}, bucket: {}", endpoint, bucketName);
    }

    private void requireClient() {
        if (minioClient == null) {
            throw new IllegalStateException("MinIO client not initialized — set OSS_ACCESS_KEY_ID and OSS_ACCESS_KEY_SECRET.");
        }
    }

    public String generatePresignedPutUrl(String objectKey) {
        requireClient();
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(300, TimeUnit.SECONDS)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL for " + objectKey, e);
        }
    }

    public AfsObjectMetadata headObject(String objectKey) {
        requireClient();
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(objectKey).build());

            AfsObjectMetadata meta = new AfsObjectMetadata();
            meta.setContentType(stat.contentType());
            meta.setContentLength(String.valueOf(stat.size()));
            return meta;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return null;
            }
            throw new RuntimeException("Failed to head object " + objectKey, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to head object " + objectKey, e);
        }
    }

    public String getObjectUrl(String objectKey) {
        return endpoint + "/" + bucketName + "/" + objectKey;
    }
}
