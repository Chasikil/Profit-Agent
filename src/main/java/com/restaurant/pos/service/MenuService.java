package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Menu;

import java.math.BigDecimal;

public interface MenuService {

    Menu getActiveMenu();

    void addDish(Dish dish);

    void updateDish(Dish dish);

    void changeDishPrice(Long dishId, BigDecimal newPrice);

    void deactivateDish(Long dishId);

    /**
     * Check if dish is available based on inventory.
     * Dish is available only if all products in its TechCard are in stock.
     *
     * @param dishId dish ID
     * @return true if dish is available (all ingredients in stock), false otherwise
     */
    boolean isDishAvailable(Long dishId);

    /**
     * Seed default menu with categories and dishes if the menu is empty.
     * Categories: Starters, Main dishes, Drinks, Desserts.
     * No duplicates on restart.
     */
    void seedDefaultMenu();
}

