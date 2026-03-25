package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.FinanceOperationType;
import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.FinanceOperation;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.service.AnalyticsService;
import com.restaurant.pos.service.FinanceService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * In-memory implementation of AnalyticsService.
 * Provides analytics based on orders and finance operations.
 */
public class InMemoryAnalyticsService implements AnalyticsService {

    private final OrderService orderService;
    private final FinanceService financeService;
    private final OrderPricingService orderPricingService;

    public InMemoryAnalyticsService(OrderService orderService,
                                    FinanceService financeService,
                                    OrderPricingService orderPricingService) {
        this.orderService = orderService;
        this.financeService = financeService;
        this.orderPricingService = orderPricingService;
    }

    @Override
    public Map<LocalDate, Integer> getOrdersCountPerDay(LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> result = new HashMap<>();
        
        if (orderService == null || start == null || end == null || end.isBefore(start)) {
            return result;
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<Order> orders = orderService.getOrdersByPeriod(startDateTime, endDateTime);
        
        for (Order order : orders) {
            if (order == null || order.getCreatedAt() == null) {
                continue;
            }
            
            // Only count paid orders
            if (order.getStatus() != OrderStatus.PAID) {
                continue;
            }

            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            if (!orderDate.isBefore(start) && !orderDate.isAfter(end)) {
                result.put(orderDate, result.getOrDefault(orderDate, 0) + 1);
            }
        }

        return result;
    }

    @Override
    public Map<LocalDate, BigDecimal> getRevenuePerDay(LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> result = new HashMap<>();
        
        if (orderService == null || orderPricingService == null || start == null || end == null || end.isBefore(start)) {
            return result;
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<Order> orders = orderService.getOrdersByPeriod(startDateTime, endDateTime);
        
        for (Order order : orders) {
            if (order == null || order.getCreatedAt() == null) {
                continue;
            }
            
            // Only count paid orders
            if (order.getStatus() != OrderStatus.PAID) {
                continue;
            }

            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            if (!orderDate.isBefore(start) && !orderDate.isAfter(end)) {
                BigDecimal revenue = orderPricingService.calculateTotal(order);
                if (revenue != null && revenue.signum() > 0) {
                    BigDecimal currentRevenue = result.getOrDefault(orderDate, BigDecimal.ZERO);
                    result.put(orderDate, currentRevenue.add(revenue));
                }
            }
        }

        return result;
    }

    @Override
    public Map<String, BigDecimal> getRevenuePerCategory(LocalDate start, LocalDate end) {
        Map<String, BigDecimal> result = new HashMap<>();
        
        if (financeService == null || start == null || end == null || end.isBefore(start)) {
            return result;
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<FinanceOperation> operations = financeService.getOperationsByDateRange(startDateTime, endDateTime);
        
        for (FinanceOperation operation : operations) {
            if (operation == null || operation.getType() != FinanceOperationType.INCOME) {
                continue;
            }

            String category = operation.getCategory();
            if (category == null || category.isEmpty()) {
                category = "UNCATEGORIZED";
            }

            BigDecimal amount = operation.getAmount();
            if (amount != null && amount.signum() > 0) {
                BigDecimal currentRevenue = result.getOrDefault(category, BigDecimal.ZERO);
                result.put(category, currentRevenue.add(amount));
            }
        }

        return result;
    }

    @Override
    public List<MenuItemSales> getTopSellingMenuItems(LocalDate start, LocalDate end, int limit) {
        List<MenuItemSales> result = new ArrayList<>();
        
        if (orderService == null || start == null || end == null || end.isBefore(start) || limit <= 0) {
            return result;
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<Order> orders = orderService.getOrdersByPeriod(startDateTime, endDateTime);
        
        // Map to aggregate sales by dish
        Map<Long, DishSalesData> dishSalesMap = new HashMap<>();
        
        for (Order order : orders) {
            if (order == null || order.getCreatedAt() == null) {
                continue;
            }
            
            // Only count paid orders
            if (order.getStatus() != OrderStatus.PAID) {
                continue;
            }

            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            if (orderDate.isBefore(start) || orderDate.isAfter(end)) {
                continue;
            }

            if (order.getItems() == null) {
                continue;
            }

            for (OrderItem item : order.getItems()) {
                if (item == null || item.getDish() == null || item.getDish().getId() == null) {
                    continue;
                }

                Dish dish = item.getDish();
                Long dishId = dish.getId();
                int quantity = item.getQuantity();
                
                if (quantity <= 0) {
                    continue;
                }

                DishSalesData salesData = dishSalesMap.getOrDefault(dishId, 
                    new DishSalesData(dishId, dish.getName(), 0, BigDecimal.ZERO));
                
                salesData.quantity += quantity;
                
                // Calculate revenue for this item
                BigDecimal itemPrice = dish.getSalePrice();
                if (itemPrice != null) {
                    BigDecimal itemRevenue = itemPrice.multiply(BigDecimal.valueOf(quantity));
                    salesData.revenue = salesData.revenue.add(itemRevenue);
                }
                
                dishSalesMap.put(dishId, salesData);
            }
        }

        // Convert to MenuItemSales and sort by quantity descending
        result = dishSalesMap.values().stream()
            .map(data -> new MenuItemSales(data.dishId, data.dishName, data.quantity, data.revenue))
            .sorted(Comparator.comparingInt(MenuItemSales::getTotalQuantity).reversed())
            .limit(limit)
            .collect(Collectors.toList());

        return result;
    }

    @Override
    public Map<String, BigDecimal> getExpensesPerCategory(LocalDate start, LocalDate end) {
        Map<String, BigDecimal> result = new HashMap<>();
        
        if (financeService == null || start == null || end == null || end.isBefore(start)) {
            return result;
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<FinanceOperation> operations = financeService.getOperationsByDateRange(startDateTime, endDateTime);
        
        for (FinanceOperation operation : operations) {
            if (operation == null || operation.getType() != FinanceOperationType.EXPENSE) {
                continue;
            }

            String category = operation.getCategory();
            if (category == null || category.isEmpty()) {
                category = "UNCATEGORIZED";
            }

            BigDecimal amount = operation.getAmount();
            if (amount != null && amount.signum() > 0) {
                BigDecimal currentExpenses = result.getOrDefault(category, BigDecimal.ZERO);
                result.put(category, currentExpenses.add(amount));
            }
        }

        return result;
    }

    @Override
    public Map<LocalDate, BigDecimal> getProfitPerDay(LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> result = new HashMap<>();
        
        if (start == null || end == null || end.isBefore(start)) {
            return result;
        }

        // Get revenue per day
        Map<LocalDate, BigDecimal> revenuePerDay = getRevenuePerDay(start, end);
        
        // Get expenses per day from finance operations
        Map<LocalDate, BigDecimal> expensesPerDay = new HashMap<>();
        
        if (financeService != null) {
            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
            
            List<FinanceOperation> operations = financeService.getOperationsByDateRange(startDateTime, endDateTime);
            
            for (FinanceOperation operation : operations) {
                if (operation == null || operation.getDateTime() == null) {
                    continue;
                }
                
                if (operation.getType() != FinanceOperationType.EXPENSE) {
                    continue;
                }

                LocalDate operationDate = operation.getDateTime().toLocalDate();
                if (!operationDate.isBefore(start) && !operationDate.isAfter(end)) {
                    BigDecimal amount = operation.getAmount();
                    if (amount != null && amount.signum() > 0) {
                        BigDecimal currentExpenses = expensesPerDay.getOrDefault(operationDate, BigDecimal.ZERO);
                        expensesPerDay.put(operationDate, currentExpenses.add(amount));
                    }
                }
            }
        }

        // Calculate profit = revenue - expenses for each day
        // Include all days from start to end
        LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            BigDecimal revenue = revenuePerDay.getOrDefault(currentDate, BigDecimal.ZERO);
            BigDecimal expenses = expensesPerDay.getOrDefault(currentDate, BigDecimal.ZERO);
            BigDecimal profit = revenue.subtract(expenses);
            result.put(currentDate, profit);
            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    /**
     * Helper class for aggregating dish sales data.
     */
    private static class DishSalesData {
        Long dishId;
        String dishName;
        int quantity;
        BigDecimal revenue;

        DishSalesData(Long dishId, String dishName, int quantity, BigDecimal revenue) {
            this.dishId = dishId;
            this.dishName = dishName != null ? dishName : "Unknown";
            this.quantity = quantity;
            this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        }
    }
}
