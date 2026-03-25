package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Order;

import java.math.BigDecimal;

public interface OrderPricingService {

    BigDecimal calculateTotal(Order order);
}

