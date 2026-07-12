package com.alipay.business.common.service.integration.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class IAgentExtractedFields {
    private String vendor;
    private String date;
    private Double amount;
    private String currency;
    private String category;
    private String description;
    @JsonProperty("tax_amount")
    private Double taxAmount;
    @JsonProperty("sst_amount")
    private Double sstAmount;
    private List<IAgentLineItem> items = new ArrayList<>();

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }
    public Double getSstAmount() { return sstAmount; }
    public void setSstAmount(Double sstAmount) { this.sstAmount = sstAmount; }
    public List<IAgentLineItem> getItems() { return items; }
    public void setItems(List<IAgentLineItem> items) { this.items = items; }
}
