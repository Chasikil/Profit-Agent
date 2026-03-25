package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.TechCard;

import java.math.BigDecimal;

/**
 * Service for managing technological cards (recipes) for dishes.
 * Stores tech cards in memory and provides lookup by dish ID.
 * No inventory logic yet.
 */
public interface TechCardService {

    /**
     * Add or update a tech card.
     * If a tech card with the same dishId exists, it will be replaced.
     * 
     * @param techCard tech card to add or update
     */
    void addTechCard(TechCard techCard);

    /**
     * Get tech card by dish ID.
     * 
     * @param dishId dish ID
     * @return tech card or null if not found
     */
    TechCard getByDishId(Long dishId);

    /**
     * Calculate total cost of dish based on tech card ingredients.
     * Cost = sum of (product.costPerUnit * quantityRequired) for all ingredients.
     * 
     * @param dishId dish ID
     * @return total cost or BigDecimal.ZERO if tech card not found or has no ingredients
     */
    BigDecimal calculateDishCost(Long dishId);
}

