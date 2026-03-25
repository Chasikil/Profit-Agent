package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.FinanceOperationType;
import com.restaurant.pos.domain.enums.InventoryOperationType;
import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.FinanceOperation;
import com.restaurant.pos.domain.model.InventoryOperation;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.domain.model.Product;
import com.restaurant.pos.domain.model.TechCard;
import com.restaurant.pos.domain.model.TechCardItem;
import com.restaurant.pos.service.FinanceService;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.TechCardService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryInventoryService implements InventoryService {

    private final Map<Long, Product> productsById = new HashMap<>();
    private final List<InventoryOperation> operations = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final TechCardService techCardService;
    private final FinanceService financeService;

    public InMemoryInventoryService() {
        this.techCardService = null;
        this.financeService = null;
    }

    public InMemoryInventoryService(TechCardService techCardService) {
        this.techCardService = techCardService;
        this.financeService = null;
    }

    public InMemoryInventoryService(TechCardService techCardService, FinanceService financeService) {
        this.techCardService = techCardService;
        this.financeService = financeService;
    }

    @Override
    public void seedDefaultInventory() {
        if (!productsById.isEmpty()) {
            return;
        }
        addProduct(createProduct("Chicken", "g", "5000", "0.02", "500"));
        addProduct(createProduct("Pasta", "g", "3000", "0.015", "300"));
        addProduct(createProduct("Cheese", "g", "2000", "0.03", "200"));
        addProduct(createProduct("Lettuce", "g", "1000", "0.01", "100"));
        addProduct(createProduct("Coffee beans", "g", "1000", "0.05", "100"));
        addProduct(createProduct("Milk", "ml", "5000", "0.01", "500"));
        addProduct(createProduct("Coca-Cola syrup", "ml", "3000", "0.008", "300"));
        addProduct(createProduct("Tomatoes", "g", "2000", "0.012", "200"));
        addProduct(createProduct("Bread", "g", "1500", "0.02", "150"));
        addProduct(createProduct("Olive oil", "ml", "1000", "0.025", "100"));
        addProduct(createProduct("Orange juice", "ml", "3000", "0.01", "300"));
    }

    private Product createProduct(String name, String unit, String qty, String costPerUnitStr, String threshold) {
        Product p = new Product();
        p.setName(name);
        p.setUnit(unit);
        p.setQuantityInStock(new BigDecimal(qty));
        p.setCostPerUnit(new BigDecimal(costPerUnitStr));
        p.setMinimumThreshold(new BigDecimal(threshold));
        return p;
    }

    @Override
    public void checkAndWriteOffForDish(Dish dish, int quantity, String reason) {
        if (dish == null || quantity <= 0 || techCardService == null) {
            return;
        }
        TechCard techCard = techCardService.getByDishId(dish.getId());
        if (techCard == null || techCard.getItems() == null || techCard.getItems().isEmpty()) {
            return;
        }
        String writeOffReason = reason != null ? reason : "Order item";
        for (TechCardItem item : techCard.getItems()) {
            if (item == null || item.getProductId() == null || item.getQuantityRequired() == null) {
                continue;
            }
            BigDecimal required = item.getQuantityRequired().multiply(BigDecimal.valueOf(quantity));
            if (!isProductAvailable(item.getProductId(), required)) {
                String name = item.getProductName() != null ? item.getProductName() : "ingredient";
                throw new RuntimeException("Not enough " + name + " in stock");
            }
        }
        writeOffByTechCard(techCard, quantity, writeOffReason);
    }

    @Override
    public boolean hasIngredientsForDish(Dish dish, int quantity) {
        if (dish == null || dish.getId() == null || quantity <= 0 || techCardService == null) {
            return false;
        }
        TechCard techCard = techCardService.getByDishId(dish.getId());
        if (techCard == null || techCard.getItems() == null || techCard.getItems().isEmpty()) {
            return false;
        }
        for (TechCardItem item : techCard.getItems()) {
            if (item == null || item.getProductId() == null || item.getQuantityRequired() == null) {
                continue;
            }
            BigDecimal required = item.getQuantityRequired().multiply(BigDecimal.valueOf(quantity));
            if (!isProductAvailable(item.getProductId(), required)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasIngredientsForOrder(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty() || techCardService == null) {
            return false;
        }
        // aggregate requirements per product
        Map<Long, BigDecimal> requiredByProduct = new HashMap<>();
        for (OrderItem orderItem : order.getItems()) {
            if (orderItem == null || orderItem.getDish() == null || orderItem.getDish().getId() == null) {
                continue;
            }
            int qty = orderItem.getQuantity();
            if (qty <= 0) continue;
            TechCard techCard = techCardService.getByDishId(orderItem.getDish().getId());
            if (techCard == null || techCard.getItems() == null) continue;
            for (TechCardItem item : techCard.getItems()) {
                if (item == null || item.getProductId() == null || item.getQuantityRequired() == null) continue;
                BigDecimal req = item.getQuantityRequired().multiply(BigDecimal.valueOf(qty));
                requiredByProduct.merge(item.getProductId(), req, BigDecimal::add);
            }
        }
        for (Map.Entry<Long, BigDecimal> e : requiredByProduct.entrySet()) {
            if (!isProductAvailable(e.getKey(), e.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void restoreForDish(Dish dish, int quantity, String reason) {
        if (dish == null || quantity <= 0 || techCardService == null) {
            return;
        }
        TechCard techCard = techCardService.getByDishId(dish.getId());
        if (techCard == null || techCard.getItems() == null) {
            return;
        }
        for (TechCardItem item : techCard.getItems()) {
            if (item == null || item.getProductId() == null || item.getQuantityRequired() == null) {
                continue;
            }
            BigDecimal restoreQty = item.getQuantityRequired().multiply(BigDecimal.valueOf(quantity));
            receiveProduct(item.getProductId(), restoreQty);
        }
    }

    @Override
    public void addProduct(Product product) {
        if (product == null) {
            return;
        }
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
        }
        if (product.getQuantityInStock() == null) {
            product.setQuantityInStock(BigDecimal.ZERO);
        }
        productsById.put(product.getId(), product);
    }

    @Override
    public void receiveProduct(Long productId, BigDecimal quantity) {
        if (productId == null || quantity == null || quantity.signum() <= 0) {
            return;
        }
        Product product = productsById.get(productId);
        if (product == null) {
            return;
        }
        BigDecimal current = product.getQuantityInStock() != null ? product.getQuantityInStock() : BigDecimal.ZERO;
        product.setQuantityInStock(current.add(quantity));
        recordOperation(product, quantity, InventoryOperationType.IN, "Receive");
    }

    @Override
    public void writeOffProduct(Long productId, BigDecimal quantity, String reason) {
        if (productId == null || quantity == null || quantity.signum() <= 0) {
            return;
        }
        Product product = productsById.get(productId);
        if (product == null) {
            return;
        }
        BigDecimal current = product.getQuantityInStock() != null ? product.getQuantityInStock() : BigDecimal.ZERO;
        BigDecimal newQty = current.subtract(quantity);
        if (newQty.signum() < 0) {
            newQty = BigDecimal.ZERO;
        }
        product.setQuantityInStock(newQty);
        recordOperation(product, quantity, InventoryOperationType.OUT, reason);

        // Add EXPENSE operation for inventory write-off (cost price)
        addExpenseOperation(product, quantity, reason);
    }

    @Override
    public BigDecimal getAvailableQuantity(Long productId) {
        Product product = productsById.get(productId);
        if (product == null || product.getQuantityInStock() == null) {
            return BigDecimal.ZERO;
        }
        return product.getQuantityInStock();
    }

    @Override
    public List<InventoryOperation> getInventoryReport() {
        return new ArrayList<>(operations);
    }

    @Override
    public List<Product> getAllProducts() {
        return new ArrayList<>(productsById.values());
    }

    @Override
    public Product getProductById(Long productId) {
        if (productId == null) {
            return null;
        }
        return productsById.get(productId);
    }

    @Override
    public boolean isProductAvailable(Long productId, BigDecimal requiredQuantity) {
        if (productId == null || requiredQuantity == null || requiredQuantity.signum() <= 0) {
            return false;
        }

        Product product = productsById.get(productId);
        if (product == null) {
            return false;
        }

        BigDecimal availableQuantity = getAvailableQuantity(productId);
        return availableQuantity.compareTo(requiredQuantity) >= 0;
    }

    @Override
    public void writeOffByTechCard(com.restaurant.pos.domain.model.TechCard techCard, int quantity, String reason) {
        if (techCard == null || techCard.getItems() == null || quantity <= 0) {
            return;
        }

        String writeOffReason = reason != null ? reason : "Tech card write-off";

        for (com.restaurant.pos.domain.model.TechCardItem item : techCard.getItems()) {
            if (item == null || item.getProductId() == null) {
                continue;
            }

            Long productId = item.getProductId();
            BigDecimal quantityRequired = item.getQuantityRequired();

            if (quantityRequired == null || quantityRequired.signum() <= 0) {
                continue;
            }

            // Multiply required quantity by order quantity (e.g., 2 portions = 2x ingredients)
            BigDecimal totalQuantity = quantityRequired.multiply(BigDecimal.valueOf(quantity));

            // Write off the ingredient
            writeOffProduct(productId, totalQuantity, writeOffReason);
        }
    }

    @Override
    public void writeOffByOrder(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }

        if (techCardService == null) {
            throw new RuntimeException("TechCardService is not available");
        }

        String orderReason = "Order #" + (order.getId() != null ? order.getId() : "unknown");

        // First pass: validate all products are available
        for (OrderItem orderItem : order.getItems()) {
            if (orderItem == null || orderItem.getDish() == null) {
                continue;
            }

            Long dishId = orderItem.getDish().getId();
            if (dishId == null) {
                continue;
            }

            TechCard techCard = techCardService.getByDishId(dishId);
            if (techCard == null || techCard.getItems() == null) {
                String dishName = orderItem.getDish().getName() != null 
                        ? orderItem.getDish().getName() 
                        : "Dish #" + dishId;
                throw new RuntimeException("Tech card not found for dish: " + dishName);
            }

            int orderItemQuantity = orderItem.getQuantity();
            if (orderItemQuantity <= 0) {
                continue;
            }

            // Check stock availability for all ingredients
            for (TechCardItem techCardItem : techCard.getItems()) {
                if (techCardItem == null || techCardItem.getProductId() == null) {
                    continue;
                }

                BigDecimal quantityRequired = techCardItem.getQuantityRequired();
                if (quantityRequired == null || quantityRequired.signum() <= 0) {
                    continue;
                }

                // Calculate total quantity needed = quantityRequired * orderItem.quantity
                BigDecimal totalQuantity = quantityRequired.multiply(BigDecimal.valueOf(orderItemQuantity));

                // Check if product is available
                if (!isProductAvailable(techCardItem.getProductId(), totalQuantity)) {
                    String productName = techCardItem.getProductName() != null 
                            ? techCardItem.getProductName() 
                            : "Product #" + techCardItem.getProductId();
                    BigDecimal available = getAvailableQuantity(techCardItem.getProductId());
                    throw new RuntimeException(
                            String.format("Insufficient stock for product '%s': required %.2f, available %.2f",
                                    productName, totalQuantity, available != null ? available : BigDecimal.ZERO));
                }
            }
        }

        // Second pass: write off products (all validations passed)
        for (OrderItem orderItem : order.getItems()) {
            if (orderItem == null || orderItem.getDish() == null) {
                continue;
            }

            Long dishId = orderItem.getDish().getId();
            if (dishId == null) {
                continue;
            }

            TechCard techCard = techCardService.getByDishId(dishId);
            if (techCard == null || techCard.getItems() == null) {
                continue;
            }

            int orderItemQuantity = orderItem.getQuantity();
            if (orderItemQuantity <= 0) {
                continue;
            }

            for (TechCardItem techCardItem : techCard.getItems()) {
                if (techCardItem == null || techCardItem.getProductId() == null) {
                    continue;
                }

                BigDecimal quantityRequired = techCardItem.getQuantityRequired();
                if (quantityRequired == null || quantityRequired.signum() <= 0) {
                    continue;
                }

                // Calculate total quantity = quantityRequired * orderItem.quantity
                BigDecimal totalQuantity = quantityRequired.multiply(BigDecimal.valueOf(orderItemQuantity));

                // Write off the product
                writeOffProduct(techCardItem.getProductId(), totalQuantity, orderReason);
            }
        }
    }

    /**
     * Add EXPENSE finance operation when inventory is written off.
     * Expense = product.costPerUnit * quantity
     * 
     * @param product product being written off
     * @param quantity quantity written off
     * @param reason reason for write-off
     */
    private void addExpenseOperation(Product product, BigDecimal quantity, String reason) {
        if (financeService == null || product == null || quantity == null || quantity.signum() <= 0) {
            return;
        }

        BigDecimal costPerUnit = product.getCostPerUnit();
        if (costPerUnit == null || costPerUnit.signum() <= 0) {
            return;
        }

        // Calculate expense: costPerUnit * quantity
        BigDecimal expenseAmount = costPerUnit.multiply(quantity);

        FinanceOperation expenseOperation = new FinanceOperation();
        expenseOperation.setType(FinanceOperationType.EXPENSE);
        expenseOperation.setAmount(expenseAmount);
        expenseOperation.setCategory("Inventory Write-off");
        String productName = product.getName() != null ? product.getName() : "Product #" + product.getId();
        expenseOperation.setDescription("Inventory write-off: " + productName + " - " + (reason != null ? reason : "Write-off"));
        expenseOperation.setDateTime(LocalDateTime.now());
        expenseOperation.setRelatedOrderId(null); // Inventory write-offs are not directly related to orders
        expenseOperation.setCreatedBy(null); // System-generated operation

        financeService.addOperation(expenseOperation);
    }

    @Override
    public List<Product> getLowStockIngredients() {
        List<Product> lowStock = new ArrayList<>();
        for (Product product : productsById.values()) {
            if (product == null) continue;
            BigDecimal quantity = product.getQuantityInStock();
            BigDecimal threshold = product.getMinimumThreshold();
            if (quantity != null && threshold != null && quantity.compareTo(threshold) <= 0) {
                lowStock.add(product);
            }
        }
        return lowStock;
    }

    private void recordOperation(Product product,
                                 BigDecimal quantity,
                                 InventoryOperationType type,
                                 String reason) {
        InventoryOperation op = new InventoryOperation();
        op.setProduct(product);
        op.setQuantity(quantity);
        op.setType(type);
        op.setTimestamp(LocalDateTime.now());
        op.setReason(reason);
        operations.add(op);
    }
}

