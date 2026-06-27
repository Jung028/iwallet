package com.alipay.business.common.service.integration.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class IAgentExtractResponse {
    @JsonProperty("source_document_id")
    private String sourceDocumentId;
    private String status;
    private IAgentExtractedFields extracted;
    @JsonProperty("missing_fields")
    private List<String> missingFields;
    @JsonProperty("clarifying_questions")
    private List<String> clarifyingQuestions;
    @JsonProperty("raw_text")
    private String rawText;

    public String getSourceDocumentId() { return sourceDocumentId; }
    public void setSourceDocumentId(String sourceDocumentId) { this.sourceDocumentId = sourceDocumentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public IAgentExtractedFields getExtracted() { return extracted; }
    public void setExtracted(IAgentExtractedFields extracted) { this.extracted = extracted; }
    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }
    public List<String> getClarifyingQuestions() { return clarifyingQuestions; }
    public void setClarifyingQuestions(List<String> clarifyingQuestions) { this.clarifyingQuestions = clarifyingQuestions; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
