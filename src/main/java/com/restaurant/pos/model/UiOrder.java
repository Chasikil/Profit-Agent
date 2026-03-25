package com.restaurant.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UI-level/order summary model used for dashboard and in-memory storage.
 * Does not depend on JavaFX and is separate from domain Order.
 */
public class UiOrder {

    private Long id;
    private Long shiftId;
    private LocalDateTime createdAt;
    private List<UiOrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public UiOrder() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<UiOrderItem> getItems() {
        return items;
    }

    public void setItems(List<UiOrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        recalculateTotal();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    /**
     * Recalculate totalAmount from items.
     */
    public void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (UiOrderItem item : items) {
                if (item != null) {
                    BigDecimal itemTotal = item.getTotal();
                    if (itemTotal != null) {
                        total = total.add(itemTotal);
                    }
                }
            }
        }
        this.totalAmount = total;
    }
}

