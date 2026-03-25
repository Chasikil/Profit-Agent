package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Dish;

import java.util.List;

public interface DishAvailabilityService {

    boolean isDishAvailable(Dish dish);

    List<Dish> getUnavailableDishes();
}

