package com.alipay.business.core.model.domain;

import java.math.BigDecimal;
import java.util.Date;

public class ReceiptItemDomain {
    private Object itemId;
    private Object receiptId;
    private String name;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String selectedBy;
    private String status;
    private String qrReferenceId;
    private Date createdAt;
    private Date updatedAt;

    public Object getItemId() { return itemId; }
    public void setItemId(Object itemId) { this.itemId = itemId; }

    public Object getReceiptId() { return receiptId; }
    public void setReceiptId(Object receiptId) { this.receiptId = receiptId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getSelectedBy() { return selectedBy; }
    public void setSelectedBy(String selectedBy) { this.selectedBy = selectedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrReferenceId() { return qrReferenceId; }
    public void setQrReferenceId(String qrReferenceId) { this.qrReferenceId = qrReferenceId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
