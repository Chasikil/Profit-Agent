package com.restaurant.pos.model;

public class OrdersCountDTO {

    private int ordersCount;

    public OrdersCountDTO() {
    }

    public OrdersCountDTO(int ordersCount) {
        this.ordersCount = ordersCount;
    }

    public int getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(int ordersCount) {
        this.ordersCount = ordersCount;
    }
}

