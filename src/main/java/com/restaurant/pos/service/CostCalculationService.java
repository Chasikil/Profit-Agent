package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.DishCost;
import com.restaurant.pos.domain.model.Order;

import java.math.BigDecimal;

public interface CostCalculationService {

    DishCost calculateDishCost(Dish dish);

    BigDecimal calculateOrderCost(Order order);
}

