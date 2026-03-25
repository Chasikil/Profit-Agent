package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.InventoryOperation;
import com.restaurant.pos.domain.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {

    /**
     * Seed default inventory if empty. Chicken, Pasta, Cheese, etc.
     */
    void seedDefaultInventory();

    /**
     * Check availability and deduct ingredients for a dish. Throws if insufficient stock.
     *
     * @param dish     dish
     * @param quantity portions
     * @param reason   reason for write-off
     * @throws RuntimeException if not enough of any ingredient
     */
    void checkAndWriteOffForDish(Dish dish, int quantity, String reason);

    /**
     * Restore ingredients when dish is removed from order or order is cancelled.
     */
    void restoreForDish(Dish dish, int quantity, String reason);

    void addProduct(Product product);

    void receiveProduct(Long productId, BigDecimal quantity);

    void writeOffProduct(Long productId, BigDecimal quantity, String reason);

    BigDecimal getAvailableQuantity(Long productId);

    List<InventoryOperation> getInventoryReport();

    /**
     * Возвращает все товары на складе для отображения в UI.
     */
    List<Product> getAllProducts();

    /**
     * Get product by ID.
     * 
     * @param productId product ID
     * @return product or null if not found
     */
    Product getProductById(Long productId);

    /**
     * Check if product is available in required quantity.
     * 
     * @param productId product ID
     * @param requiredQuantity required quantity
     * @return true if product exists and available quantity >= required quantity
     */
    boolean isProductAvailable(Long productId, BigDecimal requiredQuantity);

    /**
     * Write off products according to tech card (recipe).
     * Writes off all ingredients from tech card multiplied by quantity.
     * 
     * @param techCard technological card with ingredients
     * @param quantity multiplier for ingredient quantities (e.g., 2 for 2 portions)
     * @param reason reason for write-off (e.g., "Order #123")
     */
    void writeOffByTechCard(com.restaurant.pos.domain.model.TechCard techCard, int quantity, String reason);

    /**
     * Write off products for an order based on tech cards.
     * For each OrderItem, gets the TechCard by dishId and writes off all ingredients.
     * 
     * @param order order to process
     * @throws RuntimeException if insufficient stock for any product
     */
    void writeOffByOrder(com.restaurant.pos.domain.model.Order order);

    /**
     * Get products with low stock (quantity <= minimumThreshold).
     *
     * @return list of products with low stock
     */
    List<Product> getLowStockIngredients();

    /**
     * Check if all ingredients for a dish are available for the given quantity.
     * Does NOT write off inventory.
     *
     * @param dish dish
     * @param quantity portions
     * @return true if enough stock for all ingredients
     */
    boolean hasIngredientsForDish(Dish dish, int quantity);

    /**
     * Check if all ingredients for an order are available.
     * Does NOT write off inventory.
     *
     * @param order order
     * @return true if enough stock for all ingredients in the order
     */
    boolean hasIngredientsForOrder(com.restaurant.pos.domain.model.Order order);
}

