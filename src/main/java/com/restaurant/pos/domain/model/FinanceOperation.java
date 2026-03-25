package com.restaurant.pos.domain.model;

import com.restaurant.pos.domain.enums.FinanceOperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FinanceOperation {

    private Long id;
    private FinanceOperationType type;
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDateTime dateTime;
    private Long relatedOrderId;
    private Long createdBy;

    public FinanceOperation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FinanceOperationType getType() {
        return type;
    }

    public void setType(FinanceOperationType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Long getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Long relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @deprecated Use getDateTime() instead
     */
    @Deprecated
    public LocalDateTime getTimestamp() {
        return dateTime;
    }

    /**
     * @deprecated Use setDateTime() instead
     */
    @Deprecated
    public void setTimestamp(LocalDateTime timestamp) {
        this.dateTime = timestamp;
    }
}
