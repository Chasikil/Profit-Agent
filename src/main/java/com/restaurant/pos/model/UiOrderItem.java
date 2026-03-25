package com.restaurant.pos.model;

import java.math.BigDecimal;

/**
 * UI-level order item model used for in-memory orders.
 */
public class UiOrderItem {

    private String menuItemName;
    private int quantity;
    private BigDecimal price;

    public UiOrderItem() {
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Calculate total for this order item: quantity * price.
     *
     * @return total amount, or BigDecimal.ZERO if price is null
     */
    public BigDecimal getTotal() {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}

