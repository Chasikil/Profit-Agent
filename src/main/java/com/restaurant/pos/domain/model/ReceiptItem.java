package com.restaurant.pos.domain.model;

import java.math.BigDecimal;

public class ReceiptItem {

    private String dishName;
    private int quantity;
    private BigDecimal price = BigDecimal.ZERO;

    public ReceiptItem() {
    }

    public ReceiptItem(String dishName, int quantity, BigDecimal price) {
        this.dishName = dishName;
        this.quantity = quantity;
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
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
        this.price = price != null ? price : BigDecimal.ZERO;
    }
}

