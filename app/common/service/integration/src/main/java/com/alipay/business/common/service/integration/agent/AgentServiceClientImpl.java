package com.alipay.business.common.service.integration.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * HTTP client for the iagent Python service.
 * iagent exposes POST /api/v1/documents/extract which accepts a presigned S3 URL,
 * downloads the receipt image, runs OCR via Claude Haiku vision, and returns structured data.
 */
@Service
public class AgentServiceClientImpl implements AgentServiceClient {

    @Value("${iagent.endpoint:http://localhost:8000}")
    private String iagentEndpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OcrResult extractReceipt(String fileUrl, String mimeType) {
        ExtractReceiptRequest requestBody = new ExtractReceiptRequest();
        requestBody.setSourceDocumentId(UUID.randomUUID().toString());
        requestBody.setFileUrl(fileUrl);
        requestBody.setMimeType(mimeType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<ExtractReceiptRequest> entity = new HttpEntity<>(requestBody, headers);

        //TODO: find a way to save the extracted value to cache for specific image so if its reuploaded,
        // retrieved directly from cache.
        IAgentExtractResponse response = restTemplate.postForObject(
                iagentEndpoint + "/api/v1/documents/extract",
                entity,
                IAgentExtractResponse.class);

        return mapToOcrResult(response);
    }

    private OcrResult mapToOcrResult(IAgentExtractResponse response) {
        OcrResult result = new OcrResult();
        if (response == null || response.getExtracted() == null) {
            return result;
        }

        IAgentExtractedFields extracted = response.getExtracted();
        result.setTotalAmount(extracted.getAmount() != null
                ? BigDecimal.valueOf(extracted.getAmount()) : BigDecimal.ZERO);
        result.setCurrency(extracted.getCurrency() != null ? extracted.getCurrency() : "SGD");

        BigDecimal totalTax = BigDecimal.ZERO;
        if (extracted.getTaxLines() != null) {
            for (IAgentTaxLine taxLine : extracted.getTaxLines()) {
                totalTax = totalTax.add(BigDecimal.valueOf(taxLine.getAmount()));
            }
        }
        result.setTotalTaxAmount(totalTax);

        List<OcrResult.OcrLineItem> items = new ArrayList<>();
        if (extracted.getItems() != null) {
            for (IAgentLineItem i : extracted.getItems()) {
                OcrResult.OcrLineItem item = new OcrResult.OcrLineItem();
                item.setName(i.getName());
                item.setQuantity(i.getQuantity());
                BigDecimal unitPrice = BigDecimal.valueOf(i.getUnitPrice());
                item.setUnitPrice(unitPrice);
                // iagent does not extract a total per line item, so derive it from unit price * quantity
                item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(i.getQuantity())));
                items.add(item);
            }
        }
        result.setItems(items);
        return result;
    }
}
