package com.restaurant.pos.util;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Product;
import com.restaurant.pos.domain.model.TechCard;
import com.restaurant.pos.domain.model.TechCardItem;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.MenuService;
import com.restaurant.pos.service.TechCardService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds tech cards (recipes) for seeded dishes.
 * Maps dish names to ingredient requirements.
 */
public class TechCardSeeder {

    public static void seed(MenuService menuService, InventoryService inventoryService, TechCardService techCardService) {
        if (menuService == null || inventoryService == null || techCardService == null) {
            return;
        }
        Map<String, Long> productIdsByName = new HashMap<>();
        for (Product p : inventoryService.getAllProducts()) {
            if (p != null && p.getName() != null) {
                productIdsByName.put(p.getName().toLowerCase(), p.getId());
            }
        }
        List<Dish> dishes = menuService.getActiveMenu().getDishes();
        for (Dish dish : dishes) {
            if (dish == null || dish.getId() == null || dish.getName() == null) {
                continue;
            }
            TechCard tc = techCardService.getByDishId(dish.getId());
            if (tc != null && tc.getItems() != null && !tc.getItems().isEmpty()) {
                continue;
            }
            TechCard card = buildTechCard(dish, productIdsByName);
            if (card != null && !card.getItems().isEmpty()) {
                techCardService.addTechCard(card);
            }
        }
    }

    private static TechCard buildTechCard(Dish dish, Map<String, Long> productIds) {
        TechCard card = new TechCard();
        card.setDishId(dish.getId());
        card.setDishName(dish.getName());
        String name = dish.getName();
        if (name == null) return null;
        name = name.toLowerCase();

        if (name.contains("grilled chicken") || name.contains("chicken")) {
            addItem(card, productIds, "Chicken", "Chicken", new BigDecimal("250"));
            addItem(card, productIds, "Lettuce", "Lettuce", new BigDecimal("50"));
        } else if (name.contains("pasta carbonara") || name.contains("carbonara")) {
            addItem(card, productIds, "Pasta", "Pasta", new BigDecimal("200"));
            addItem(card, productIds, "Cheese", "Cheese", new BigDecimal("50"));
            addItem(card, productIds, "Chicken", "Chicken", new BigDecimal("100"));
        } else if (name.contains("espresso")) {
            addItem(card, productIds, "Coffee beans", "Coffee beans", new BigDecimal("10"));
        } else if (name.contains("coca-cola") || name.contains("coca cola")) {
            addItem(card, productIds, "Coca-Cola syrup", "Coca-Cola syrup", new BigDecimal("200"));
        } else if (name.contains("caesar") || name.contains("salad")) {
            addItem(card, productIds, "Lettuce", "Lettuce", new BigDecimal("80"));
            addItem(card, productIds, "Cheese", "Cheese", new BigDecimal("30"));
        } else if (name.contains("bruschetta")) {
            addItem(card, productIds, "Bread", "Bread", new BigDecimal("50"));
            addItem(card, productIds, "Tomatoes", "Tomatoes", new BigDecimal("60"));
            addItem(card, productIds, "Olive oil", "Olive oil", new BigDecimal("15"));
        } else if (name.contains("caprese")) {
            addItem(card, productIds, "Tomatoes", "Tomatoes", new BigDecimal("80"));
            addItem(card, productIds, "Cheese", "Cheese", new BigDecimal("50"));
        } else if (name.contains("orange juice")) {
            addItem(card, productIds, "Orange juice", "Orange juice", new BigDecimal("200"));
        } else if (name.contains("ice cream") || name.contains("cheesecake") || name.contains("tiramisu")) {
            addItem(card, productIds, "Milk", "Milk", new BigDecimal("100"));
            addItem(card, productIds, "Cheese", "Cheese", new BigDecimal("30"));
        } else {
            addItem(card, productIds, "Lettuce", "Lettuce", new BigDecimal("30"));
        }
        return card;
    }

    private static void addItem(TechCard card, Map<String, Long> productIds, String productName, String displayName, BigDecimal qty) {
        Long id = productIds.get(productName.toLowerCase());
        if (id == null) return;
        TechCardItem item = new TechCardItem();
        item.setProductId(id);
        item.setProductName(displayName);
        item.setQuantityRequired(qty);
        card.getItems().add(item);
    }
}
