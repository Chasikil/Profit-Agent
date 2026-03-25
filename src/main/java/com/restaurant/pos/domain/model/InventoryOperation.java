package com.restaurant.pos.domain.model;

import com.restaurant.pos.domain.enums.InventoryOperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryOperation {

    private Product product;
    private BigDecimal quantity;
    private InventoryOperationType type;
    private LocalDateTime timestamp;
    private String reason;

    public InventoryOperation() {
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public InventoryOperationType getType() {
        return type;
    }

    public void setType(InventoryOperationType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

