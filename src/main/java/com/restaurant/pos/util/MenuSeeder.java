package com.restaurant.pos.util;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.service.MenuService;

import java.math.BigDecimal;

/**
 * Seeds the menu with default categories and dishes on application startup.
 */
public class MenuSeeder {

    private final MenuService menuService;

    public MenuSeeder(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * Seed the menu with default data.
     * Adds categories and menu items if the menu is empty or doesn't contain seeded items.
     * Ensures no duplicates are created.
     */
    public void seed() {
        // Check if menu already has seeded items by looking for a known seeded dish
        if (!hasSeededItems()) {
            seedCategories();
            seedMenuItems();
        }
    }
    
    /**
     * Check if menu already contains seeded items.
     * Uses "Caesar Salad" as a marker dish to determine if seeding has already occurred.
     * 
     * @return true if seeded items exist, false otherwise
     */
    private boolean hasSeededItems() {
        var dishes = menuService.getActiveMenu().getDishes();
        
        // If menu is empty, definitely not seeded
        if (dishes.isEmpty()) {
            return false;
        }
        
        // Check if any of our seeded dishes already exist
        // Using "Caesar Salad" as a marker since it's the first item we seed
        return dishes.stream()
                .anyMatch(dish -> dish != null && 
                        dish.getName() != null && 
                        dish.getName().equals("Caesar Salad"));
    }

    /**
     * Seed default categories.
     * Categories are stored as part of dish data.
     */
    private void seedCategories() {
        // Categories will be assigned to dishes
        // No separate category storage needed
    }

    /**
     * Seed menu items with default data.
     */
    private void seedMenuItems() {
        // Starters
        addDish("Caesar Salad", "Starters", "Fresh romaine lettuce with homemade Caesar dressing, parmesan cheese, and crispy croutons", new BigDecimal("450.00"));
        addDish("Soup of the Day", "Starters", "Chef's daily special soup - ask your server for today's selection", new BigDecimal("320.00"));
        addDish("Bruschetta", "Starters", "Toasted Italian bread topped with fresh tomatoes, garlic, basil, and olive oil", new BigDecimal("280.00"));
        addDish("Caprese Salad", "Starters", "Fresh mozzarella, tomatoes, and basil drizzled with balsamic glaze", new BigDecimal("380.00"));

        // Main dishes
        addDish("Grilled Chicken", "Main dishes", "Tender grilled chicken breast served with roasted vegetables and mashed potatoes", new BigDecimal("750.00"));
        addDish("Pasta Carbonara", "Main dishes", "Classic Italian pasta with bacon, eggs, parmesan cheese, and black pepper", new BigDecimal("680.00"));
        addDish("Beef Steak", "Main dishes", "Premium ribeye steak cooked to your preference, served with vegetables and fries", new BigDecimal("1200.00"));
        addDish("Grilled Salmon", "Main dishes", "Fresh Atlantic salmon with lemon butter sauce, rice, and seasonal vegetables", new BigDecimal("890.00"));
        addDish("Pizza Margherita", "Main dishes", "Traditional Italian pizza with tomato sauce, fresh mozzarella, and basil", new BigDecimal("550.00"));
        addDish("Beef Burger", "Main dishes", "Juicy beef patty with lettuce, tomato, onion, pickles, and special sauce", new BigDecimal("650.00"));

        // Drinks
        addDish("Coca-Cola", "Drinks", "Classic carbonated soft drink", new BigDecimal("150.00"));
        addDish("Espresso", "Drinks", "Strong Italian coffee served in a small cup", new BigDecimal("180.00"));
        addDish("Orange Juice", "Drinks", "Freshly squeezed orange juice", new BigDecimal("200.00"));
        addDish("Mineral Water", "Drinks", "Still or sparkling mineral water", new BigDecimal("120.00"));
        addDish("Iced Tea", "Drinks", "Refreshing iced tea with lemon", new BigDecimal("150.00"));

        // Desserts
        addDish("Cheesecake", "Desserts", "Creamy New York style cheesecake with berry compote", new BigDecimal("420.00"));
        addDish("Ice Cream", "Desserts", "Two scoops of premium ice cream - choose from vanilla, chocolate, or strawberry", new BigDecimal("250.00"));
        addDish("Tiramisu", "Desserts", "Classic Italian dessert with coffee-soaked ladyfingers and mascarpone", new BigDecimal("450.00"));
    }

    /**
     * Helper method to create and add a dish.
     * Checks if a dish with the same name already exists to prevent duplicates.
     */
    private void addDish(String name, String category, String description, BigDecimal price) {
        // Check if dish with this name already exists
        boolean dishExists = menuService.getActiveMenu().getDishes().stream()
                .anyMatch(dish -> dish != null && 
                        dish.getName() != null && 
                        dish.getName().equals(name));
        
        if (dishExists) {
            // Dish already exists, skip adding
            return;
        }
        
        Dish dish = new Dish();
        dish.setName(name);
        dish.setCategory(category);
        dish.setDescription(description);
        dish.setSalePrice(price);
        dish.setActive(true);
        menuService.addDish(dish);
    }
}
