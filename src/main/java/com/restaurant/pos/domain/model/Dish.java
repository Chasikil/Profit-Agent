package com.restaurant.pos.domain.model;

import com.restaurant.pos.service.TechCardService;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Dish {

    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal salePrice;
    private boolean active;

    public Dish() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Calculate real dish cost based on ingredient usage from tech card.
     * Cost = sum of (requiredQuantity * ingredient.costPerUnit) for each ingredient.
     *
     * @param techCardService service to get recipe and ingredient costs
     * @return total dish cost, or BigDecimal.ZERO if unavailable
     */
    public BigDecimal calculateCost(TechCardService techCardService) {
        if (techCardService == null || id == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal cost = techCardService.calculateDishCost(id);
        return cost != null ? cost : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        String displayName = name != null ? name : "";
        BigDecimal price = salePrice != null ? salePrice : BigDecimal.ZERO;
        BigDecimal rounded = price.setScale(0, RoundingMode.HALF_UP);
        return displayName + " - ₽ " + rounded.toPlainString();
    }
}

