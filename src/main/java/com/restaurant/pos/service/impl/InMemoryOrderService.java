package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.FinanceCategory;
import com.restaurant.pos.domain.enums.FinanceOperationType;
import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.enums.PaymentMethod;
import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.FinanceOperation;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.domain.model.TechCard;
import com.restaurant.pos.service.FinanceService;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;
import com.restaurant.pos.service.ShiftService;
import com.restaurant.pos.service.TechCardService;
import com.restaurant.pos.db.OrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of OrderService.
 * Integrates with TechCardService and InventoryService for ingredient management.
 */
public class InMemoryOrderService implements OrderService {

    private final Map<Long, Order> ordersById = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final TechCardService techCardService;
    private final InventoryService inventoryService;
    private final FinanceService financeService;
    private final OrderPricingService orderPricingService;
    private final ShiftService shiftService;
    private OrderRepository orderRepository;

    public InMemoryOrderService() {
        this.techCardService = null;
        this.inventoryService = null;
        this.financeService = null;
        this.orderPricingService = null;
        this.shiftService = null;
    }

    public InMemoryOrderService(TechCardService techCardService, InventoryService inventoryService) {
        this.techCardService = techCardService;
        this.inventoryService = inventoryService;
        this.financeService = null;
        this.orderPricingService = null;
        this.shiftService = null;
    }

    public InMemoryOrderService(TechCardService techCardService, InventoryService inventoryService,
                                FinanceService financeService, OrderPricingService orderPricingService) {
        this.techCardService = techCardService;
        this.inventoryService = inventoryService;
        this.financeService = financeService;
        this.orderPricingService = orderPricingService;
        this.shiftService = null;
    }

    public InMemoryOrderService(TechCardService techCardService, InventoryService inventoryService,
                                FinanceService financeService, OrderPricingService orderPricingService,
                                ShiftService shiftService) {
        this.techCardService = techCardService;
        this.inventoryService = inventoryService;
        this.financeService = financeService;
        this.orderPricingService = orderPricingService;
        this.shiftService = shiftService;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order createOrder(Employee waiter) {
        if (waiter == null || waiter.getId() == null) {
            throw new IllegalArgumentException("Waiter must be selected before creating an order");
        }
        Order order = new Order();
        if (orderRepository == null) {
            order.setId(idGenerator.getAndIncrement());
        }
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.OPEN);
        order.setWaiter(waiter);
        order.setWaiterId(waiter.getId());
        persistOrder(order);
        if (order.getId() != null) {
            ordersById.put(order.getId(), order);
        }
        return order;
    }

    @Override
    public void addDish(Order order, Dish dish, int quantity) {
        if (order == null || dish == null || quantity <= 0) {
            return;
        }
        List<OrderItem> items = order.getItems();
        for (OrderItem item : items) {
            if (item.getDish() != null && item.getDish().getId() != null
                    && item.getDish().getId().equals(dish.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                persistOrder(order);
                return;
            }
        }
        OrderItem newItem = new OrderItem();
        newItem.setDish(dish);
        newItem.setQuantity(quantity);
        items.add(newItem);
        persistOrder(order);
    }

    @Override
    public void removeDish(Order order, Dish dish) {
        if (order == null || dish == null || dish.getId() == null) {
            return;
        }
        List<OrderItem> items = order.getItems();
        items.removeIf(item ->
                item.getDish() != null
                        && item.getDish().getId() != null
                        && item.getDish().getId().equals(dish.getId())
        );
        persistOrder(order);
    }

    @Override
    public Order getOrderById(Long orderId) {
        Order o = ordersById.get(orderId);
        if (o == null && orderRepository != null) {
            o = orderRepository.findById(orderId);
            if (o != null) ordersById.put(o.getId(), o);
        }
        return o;
    }

    @Override
    public List<Order> getOrdersByWaiter(Long waiterId) {
        List<Order> result = new ArrayList<>();
        if (waiterId == null) {
            return result;
        }
        for (Order order : ordersById.values()) {
            if (waiterId.equals(order.getWaiterId())) {
                result.add(order);
            }
        }
        return result;
    }

    @Override
    public List<Order> getOrdersByPeriod(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return new ArrayList<>();
        }
        if (orderRepository != null) {
            List<Order> fromDb = orderRepository.findByFilter(start, end, null, null);
            for (Order o : fromDb) {
                if (o != null && o.getId() != null) {
                    ordersById.put(o.getId(), o);
                }
            }
            return fromDb;
        }
        List<Order> result = new ArrayList<>();
        for (Order order : ordersById.values()) {
            LocalDateTime createdAt = order.getCreatedAt();
            if (createdAt != null && !createdAt.isBefore(start) && !createdAt.isAfter(end)) {
                result.add(order);
            }
        }
        return result;
    }

    @Override
    public void payOrder(Order order, PaymentMethod method, BigDecimal amountPaid) {
        if (order == null) {
            throw new IllegalArgumentException("Order is null");
        }
        if (order.isPaid()) {
            throw new IllegalArgumentException("Order is already paid");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        BigDecimal total = orderPricingService != null ? orderPricingService.calculateTotal(order) : null;
        if (total == null || total.signum() < 0) {
            total = BigDecimal.ZERO;
        }
        if (method == PaymentMethod.CASH) {
            if (amountPaid == null || amountPaid.compareTo(total) < 0) {
                throw new IllegalArgumentException("Insufficient cash: amount paid must be >= order total (₽ " + total.setScale(2, RoundingMode.HALF_UP) + ")");
            }
            order.setAmountPaid(amountPaid);
            order.setChange(amountPaid.subtract(total).setScale(2, RoundingMode.HALF_UP));
        } else {
            order.setAmountPaid(total);
            order.setChange(BigDecimal.ZERO);
        }
        order.setPaymentMethod(method);
        order.setPaid(true);
        addIncomeOperation(order);
        persistOrder(order);
    }

    @Override
    public CloseOrderResult closeOrder(Order order) {
        if (order == null) {
            return CloseOrderResult.failure("Order is null");
        }

        if (!order.isPaid()) {
            return CloseOrderResult.failure("Order must be paid before closing. Use Pay & Close Order.");
        }

        if (order.getStatus() != OrderStatus.OPEN) {
            return CloseOrderResult.failure("Order is already closed or canceled");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            return CloseOrderResult.failure("Cannot close empty order");
        }

        // Write off inventory for the whole order (checks stock and deducts)
        if (inventoryService != null) {
            try {
                inventoryService.writeOffByOrder(order);
            } catch (RuntimeException e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Недостаточно ингредиентов на складе";
                return CloseOrderResult.failure(msg);
            }
        }

        // Calculate costPrice, margin, totalCost, totalProfit
        calculateOrderCostAndMargin(order);

        // Record profit analytics (PAID orders only, once per order)
        if (financeService != null && orderPricingService != null) {
            BigDecimal revenue = orderPricingService.calculateTotal(order);
            if (revenue != null && revenue.signum() >= 0) {
                financeService.recordPaidOrderProfit(order, revenue);
            }
        }

        // INCOME already recorded in payOrder()

        // Update order status and closedAt
        order.setStatus(OrderStatus.PAID);
        order.setClosedAt(LocalDateTime.now());

        // Attach order to active shift and update shift totalRevenue
        attachOrderToShift(order);
        persistOrder(order);

        // Generate receipt PDF asynchronously
        generateReceiptAsync(order);

        return CloseOrderResult.success();
    }

    @Override
    public void saveOrder(Order order) {
        if (order == null) return;
        persistOrder(order);
        if (order.getId() != null) {
            ordersById.put(order.getId(), order);
        }
    }

    private void generateReceiptAsync(Order order) {
        new Thread(() -> {
            try {
                com.restaurant.pos.util.ReceiptGenerator.generateReceipt(order, orderPricingService);
            } catch (Exception e) {
                System.err.println("Failed to generate receipt: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public List<Order> getAllOrders() {
        if (orderRepository != null) {
            List<Order> fromDb = orderRepository.findAll();
            for (Order o : fromDb) {
                if (o != null && o.getId() != null) {
                    ordersById.put(o.getId(), o);
                }
            }
            return fromDb;
        }
        return new ArrayList<>(ordersById.values());
    }

    @Override
    public boolean hasActiveOrder(int tableNumber) {
        if (tableNumber <= 0) {
            return false;
        }
        // Check in-memory cache first
        for (Order order : ordersById.values()) {
            if (order == null || order.getTable() == null) continue;
            if (order.getTable().getNumber() == tableNumber && order.getStatus() == OrderStatus.OPEN) {
                return true;
            }
        }
        // Fallback to DB if repository available
        if (orderRepository != null) {
            for (Order o : orderRepository.findAll()) {
                if (o == null || o.getTable() == null) continue;
                if (o.getTable().getNumber() == tableNumber && o.getStatus() == OrderStatus.OPEN) {
                    return true;
                }
            }
        }
        return false;
    }

    private void persistOrder(Order order) {
        if (orderRepository != null && order != null) {
            orderRepository.save(order);
        }
    }

    @Override
    public void cancelOrder(Order order) {
        if (order == null) return;
        order.setStatus(OrderStatus.CANCELLED);
        order.setClosedAt(LocalDateTime.now());
        persistOrder(order);
        ordersById.remove(order.getId());
    }

    /**
     * Calculate costPrice and margin for the order.
     * costPrice = sum of (dishCost * quantity) for all items
     * margin = totalRevenue - costPrice
     * 
     * @param order order to calculate costs for
     */
    private void calculateOrderCostAndMargin(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            order.setCostPrice(BigDecimal.ZERO);
            order.setMargin(BigDecimal.ZERO);
            return;
        }

        if (techCardService == null) {
            order.setCostPrice(BigDecimal.ZERO);
            order.setMargin(BigDecimal.ZERO);
            return;
        }

        BigDecimal totalCostPrice = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (OrderItem orderItem : order.getItems()) {
            if (orderItem == null || orderItem.getDish() == null) {
                continue;
            }

            Dish dish = orderItem.getDish();
            Long dishId = dish.getId();
            int quantity = orderItem.getQuantity();

            if (dishId == null || quantity <= 0) {
                continue;
            }

            // Calculate cost price: dishCost * quantity
            BigDecimal dishCost = techCardService.calculateDishCost(dishId);
            if (dishCost == null) {
                dishCost = BigDecimal.ZERO;
            }
            BigDecimal itemCostPrice = dishCost.multiply(BigDecimal.valueOf(quantity));
            totalCostPrice = totalCostPrice.add(itemCostPrice);

            // Calculate revenue: salePrice * quantity
            BigDecimal salePrice = dish.getSalePrice();
            if (salePrice != null) {
                BigDecimal itemRevenue = salePrice.multiply(BigDecimal.valueOf(quantity));
                totalRevenue = totalRevenue.add(itemRevenue);
            }
        }

        // Set costPrice, totalCost, margin, totalProfit
        order.setCostPrice(totalCostPrice);
        order.setTotalCost(totalCostPrice);
        BigDecimal profit = totalRevenue.subtract(totalCostPrice);
        order.setMargin(profit);
        order.setTotalProfit(profit);
    }

    /**
     * Check if dish is available (all ingredients exist in required quantity).
     * 
     * @param dishId dish ID
     * @return true if dish is available, false otherwise
     */
    private boolean isDishAvailable(Long dishId) {
        if (dishId == null || techCardService == null || inventoryService == null) {
            return false;
        }

        TechCard techCard = techCardService.getByDishId(dishId);
        if (techCard == null || techCard.getItems() == null || techCard.getItems().isEmpty()) {
            return false;
        }

        // Check if all products in tech card are available
        for (com.restaurant.pos.domain.model.TechCardItem item : techCard.getItems()) {
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

        return true;
    }

    /**
     * Add INCOME finance operation when order is submitted.
     * 
     * @param order order that was submitted
     */
    private void addIncomeOperation(Order order) {
        if (financeService == null || orderPricingService == null) {
            return;
        }

        BigDecimal totalRevenue = orderPricingService.calculateTotal(order);
        if (totalRevenue == null || totalRevenue.signum() <= 0) {
            return;
        }

        FinanceOperation incomeOperation = new FinanceOperation();
        incomeOperation.setType(FinanceOperationType.INCOME);
        incomeOperation.setCategory(FinanceCategory.FOOD_SALES.name());
        incomeOperation.setAmount(totalRevenue);
        incomeOperation.setRelatedOrderId(order.getId());
        incomeOperation.setDescription("Order #" + (order.getId() != null ? order.getId() : "unknown"));
        incomeOperation.setDateTime(LocalDateTime.now());
        incomeOperation.setCreatedBy(order.getWaiterId());

        financeService.addOperation(incomeOperation);
    }

    /**
     * Attach order to active shift and update shift totalRevenue.
     * 
     * @param order order to attach
     */
    private void attachOrderToShift(Order order) {
        if (shiftService == null || order == null) {
            return;
        }

        // Attach order to active shift (this also updates shift totalRevenue)
        shiftService.attachOrderToActiveShift(order);
    }
}

