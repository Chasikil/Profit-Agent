package com.restaurant.pos.domain.model;

import java.math.BigDecimal;

/**
 * Represents product consumption per dish in a tech card.
 * Contains product reference and required quantity for recipe.
 */
public class TechCardItem {

    private Long productId;
    private String productName;
    private BigDecimal quantityRequired;

    public TechCardItem() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
    }
}

