package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.service.OrderPricingService;

import java.math.BigDecimal;

public class SimpleOrderPricingService implements OrderPricingService {

    @Override
    public BigDecimal calculateTotal(Order order) {
        if (order == null || order.getItems() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getDish() == null || item.getDish().getSalePrice() == null) {
                continue;
            }
            BigDecimal price = item.getDish().getSalePrice();
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            total = total.add(price.multiply(quantity));
        }
        return total;
    }
}

