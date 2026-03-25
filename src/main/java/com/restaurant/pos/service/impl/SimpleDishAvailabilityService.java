package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Menu;
import com.restaurant.pos.domain.model.TechCard;
import com.restaurant.pos.domain.model.TechCardItem;
import com.restaurant.pos.service.DishAvailabilityService;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.MenuService;
import com.restaurant.pos.service.TechCardService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SimpleDishAvailabilityService implements DishAvailabilityService {

    private final InventoryService inventoryService;
    private final TechCardService techCardService;
    private final MenuService menuService;

    public SimpleDishAvailabilityService(InventoryService inventoryService,
                                         TechCardService techCardService,
                                         MenuService menuService) {
        this.inventoryService = inventoryService;
        this.techCardService = techCardService;
        this.menuService = menuService;
    }

    @Override
    public boolean isDishAvailable(Dish dish) {
        if (dish == null || dish.getId() == null) {
            return false;
        }
        TechCard techCard = techCardService.getByDishId(dish.getId());
        if (techCard == null || techCard.getItems() == null) {
            return false;
        }
        for (TechCardItem item : techCard.getItems()) {
            if (item == null || item.getProductId() == null) {
                return false;
            }
            BigDecimal required = item.getQuantityRequired();
            if (required == null || required.signum() <= 0) {
                continue;
            }
            BigDecimal available = inventoryService.getAvailableQuantity(item.getProductId());
            if (available == null || available.compareTo(required) < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Dish> getUnavailableDishes() {
        List<Dish> result = new ArrayList<>();
        Menu menu = menuService.getActiveMenu();
        if (menu == null || menu.getDishes() == null) {
            return result;
        }
        for (Dish dish : menu.getDishes()) {
            if (!isDishAvailable(dish)) {
                result.add(dish);
            }
        }
        return result;
    }
}

