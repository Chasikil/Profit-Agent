package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Product;
import com.restaurant.pos.domain.model.TechCard;
import com.restaurant.pos.domain.model.TechCardItem;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.TechCardService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of TechCardService.
 * Stores tech cards in memory and provides lookup by dish ID.
 * No inventory logic yet.
 */
public class InMemoryTechCardService implements TechCardService {

    private final Map<Long, TechCard> techCardsById = new HashMap<>();
    private final Map<Long, TechCard> techCardsByDishId = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final InventoryService inventoryService;

    public InMemoryTechCardService() {
        this.inventoryService = null;
    }

    public InMemoryTechCardService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void addTechCard(TechCard techCard) {
        if (techCard == null) {
            return;
        }

        // Generate ID if not set
        if (techCard.getId() == null) {
            techCard.setId(idGenerator.getAndIncrement());
        }

        // If dishId is set, update the mapping
        if (techCard.getDishId() != null) {
            // Remove old mapping if exists
            TechCard existing = techCardsByDishId.get(techCard.getDishId());
            if (existing != null && !existing.getId().equals(techCard.getId())) {
                techCardsById.remove(existing.getId());
            }
            techCardsByDishId.put(techCard.getDishId(), techCard);
        }

        // Store by ID
        techCardsById.put(techCard.getId(), techCard);
    }

    @Override
    public TechCard getByDishId(Long dishId) {
        if (dishId == null) {
            return null;
        }
        return techCardsByDishId.get(dishId);
    }

    @Override
    public BigDecimal calculateDishCost(Long dishId) {
        if (dishId == null) {
            return BigDecimal.ZERO;
        }

        TechCard techCard = getByDishId(dishId);
        if (techCard == null || techCard.getItems() == null || techCard.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        if (inventoryService == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        for (TechCardItem item : techCard.getItems()) {
            if (item == null || item.getProductId() == null) {
                continue;
            }

            Product product = inventoryService.getProductById(item.getProductId());
            if (product == null) {
                continue;
            }

            BigDecimal quantityRequired = item.getQuantityRequired();
            if (quantityRequired == null || quantityRequired.signum() <= 0) {
                continue;
            }

            BigDecimal costPerUnit = product.getCostPerUnit();
            if (costPerUnit == null) {
                costPerUnit = BigDecimal.ZERO;
            }

            // Calculate: costPerUnit * quantityRequired
            BigDecimal itemCost = costPerUnit.multiply(quantityRequired);
            totalCost = totalCost.add(itemCost);
        }

        return totalCost;
    }
}

