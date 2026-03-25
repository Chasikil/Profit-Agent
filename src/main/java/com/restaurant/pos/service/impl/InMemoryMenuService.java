package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Menu;
import com.restaurant.pos.domain.model.TechCard;
import com.restaurant.pos.domain.model.TechCardItem;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.MenuService;
import com.restaurant.pos.service.TechCardService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryMenuService implements MenuService {

    private final Map<Long, Dish> dishesById = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Menu activeMenu = new Menu();
    private final TechCardService techCardService;
    private final InventoryService inventoryService;

    public InMemoryMenuService() {
        this.techCardService = null;
        this.inventoryService = null;
        activeMenu.setActive(true);
    }

    public InMemoryMenuService(TechCardService techCardService, InventoryService inventoryService) {
        this.techCardService = techCardService;
        this.inventoryService = inventoryService;
        activeMenu.setActive(true);
    }

    @Override
    public Menu getActiveMenu() {
        // Обновляем список меню из текущих блюд
        Collection<Dish> values = dishesById.values();
        activeMenu.getDishes().clear();
        activeMenu.getDishes().addAll(values);
        return activeMenu;
    }

    @Override
    public void addDish(Dish dish) {
        if (dish == null) {
            return;
        }
        if (dish.getId() == null) {
            dish.setId(idGenerator.getAndIncrement());
        }
        if (!dish.isActive()) {
            dish.setActive(true);
        }
        dishesById.put(dish.getId(), dish);
    }

    @Override
    public void updateDish(Dish dish) {
        if (dish == null || dish.getId() == null) {
            return;
        }
        if (!dishesById.containsKey(dish.getId())) {
            return;
        }
        dishesById.put(dish.getId(), dish);
    }

    @Override
    public void changeDishPrice(Long dishId, BigDecimal newPrice) {
        Dish existing = dishesById.get(dishId);
        if (existing == null || newPrice == null) {
            return;
        }
        existing.setSalePrice(newPrice);
    }

    @Override
    public void deactivateDish(Long dishId) {
        Dish existing = dishesById.get(dishId);
        if (existing == null) {
            return;
        }
        existing.setActive(false);
    }

    @Override
    public boolean isDishAvailable(Long dishId) {
        if (dishId == null) {
            return false;
        }

        // If services are not available, consider dish as unavailable
        if (techCardService == null || inventoryService == null) {
            return false;
        }

        // Get tech card for the dish
        TechCard techCard = techCardService.getByDishId(dishId);
        if (techCard == null || techCard.getItems() == null || techCard.getItems().isEmpty()) {
            // No tech card means dish cannot be prepared
            return false;
        }

        // Check if all products in tech card are available
        for (TechCardItem item : techCard.getItems()) {
            if (item == null || item.getProductId() == null) {
                continue;
            }

            BigDecimal quantityRequired = item.getQuantityRequired();
            if (quantityRequired == null || quantityRequired.signum() <= 0) {
                continue;
            }

            // Check if product is available in required quantity
            if (!inventoryService.isProductAvailable(item.getProductId(), quantityRequired)) {
                return false;
            }
        }

        // All products are available
        return true;
    }

    @Override
    public void seedDefaultMenu() {
        if (!dishesById.isEmpty()) {
            return;
        }
        // Starters
        addDish(createDish("Caesar Salad", "Starters", "Fresh romaine lettuce with Caesar dressing, parmesan, croutons", new BigDecimal("450.00")));
        addDish(createDish("Soup of the Day", "Starters", "Chef's daily special soup", new BigDecimal("320.00")));
        addDish(createDish("Bruschetta", "Starters", "Toasted bread with tomatoes, garlic, basil, olive oil", new BigDecimal("280.00")));
        addDish(createDish("Caprese Salad", "Starters", "Mozzarella, tomatoes, basil, balsamic glaze", new BigDecimal("380.00")));
        // Main dishes
        addDish(createDish("Grilled Chicken", "Main dishes", "Chicken breast with roasted vegetables and mashed potatoes", new BigDecimal("750.00")));
        addDish(createDish("Pasta Carbonara", "Main dishes", "Pasta with bacon, eggs, parmesan, black pepper", new BigDecimal("680.00")));
        addDish(createDish("Beef Steak", "Main dishes", "Ribeye steak with vegetables and fries", new BigDecimal("1200.00")));
        addDish(createDish("Grilled Salmon", "Main dishes", "Atlantic salmon, lemon butter, rice, vegetables", new BigDecimal("890.00")));
        addDish(createDish("Pizza Margherita", "Main dishes", "Tomato sauce, mozzarella, basil", new BigDecimal("550.00")));
        addDish(createDish("Beef Burger", "Main dishes", "Beef patty with lettuce, tomato, special sauce", new BigDecimal("650.00")));
        // Drinks
        addDish(createDish("Coca-Cola", "Drinks", "Classic soft drink", new BigDecimal("150.00")));
        addDish(createDish("Espresso", "Drinks", "Strong Italian coffee", new BigDecimal("180.00")));
        addDish(createDish("Orange Juice", "Drinks", "Freshly squeezed orange juice", new BigDecimal("200.00")));
        addDish(createDish("Mineral Water", "Drinks", "Still or sparkling water", new BigDecimal("120.00")));
        addDish(createDish("Iced Tea", "Drinks", "Iced tea with lemon", new BigDecimal("150.00")));
        // Desserts
        addDish(createDish("Cheesecake", "Desserts", "New York style cheesecake with berry compote", new BigDecimal("420.00")));
        addDish(createDish("Ice Cream", "Desserts", "Two scoops - vanilla, chocolate or strawberry", new BigDecimal("250.00")));
        addDish(createDish("Tiramisu", "Desserts", "Coffee-soaked ladyfingers, mascarpone", new BigDecimal("450.00")));
    }

    private Dish createDish(String name, String category, String description, BigDecimal price) {
        Dish d = new Dish();
        d.setName(name);
        d.setCategory(category);
        d.setDescription(description);
        d.setSalePrice(price);
        d.setActive(true);
        return d;
    }
}

