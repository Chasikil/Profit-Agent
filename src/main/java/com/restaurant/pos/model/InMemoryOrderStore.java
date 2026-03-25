package com.restaurant.pos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple in-memory order store for UI/application layer.
 * Stores orders in memory only (no persistence).
 */
public class InMemoryOrderStore {

    private final List<UiOrder> orders = new ArrayList<>();

    /**
     * Add an order to the store.
     *
     * @param order order to add
     */
    public void addOrder(UiOrder order) {
        if (order != null) {
            orders.add(order);
        }
    }

    /**
     * Get all orders from the store.
     *
     * @return unmodifiable copy of orders list
     */
    public List<UiOrder> getOrders() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }
}

