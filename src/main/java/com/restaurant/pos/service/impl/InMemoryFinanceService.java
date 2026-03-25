package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.FinanceOperationType;
import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.FinanceOperation;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.service.FinanceService;
import com.restaurant.pos.service.TechCardService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of FinanceService.
 * Stores finance operations and provides summary calculations.
 * Tracks profit analytics for PAID orders only.
 */
public class InMemoryFinanceService implements FinanceService {

    private final List<FinanceOperation> operations = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final TechCardService techCardService;

    /** Order IDs already recorded for profit (prevents double-counting). */
    private final java.util.Set<Long> recordedOrderIds = new java.util.HashSet<>();

    /** Profit analytics (PAID orders only). */
    private BigDecimal totalRevenueFromOrders = BigDecimal.ZERO;
    private BigDecimal totalCostFromOrders = BigDecimal.ZERO;
    private BigDecimal totalProfitFromOrders = BigDecimal.ZERO;
    private final Map<Long, DishStatsMutable> dishStatsMap = new HashMap<>();
    private final Map<Long, BigDecimal> profitByWaiter = new HashMap<>();

    public InMemoryFinanceService() {
        this.techCardService = null;
    }

    public InMemoryFinanceService(TechCardService techCardService) {
        this.techCardService = techCardService;
    }

    @Override
    public void addOperation(FinanceOperation operation) {
        if (operation != null) {
            // Auto-generate ID if not set
            if (operation.getId() == null) {
                operation.setId(idGenerator.getAndIncrement());
            }
            operations.add(operation);
        }
    }

    @Override
    public boolean deleteOperation(Long operationId) {
        if (operationId == null) {
            return false;
        }
        return operations.removeIf(op -> op != null && operationId.equals(op.getId()));
    }

    @Override
    public List<FinanceOperation> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(operations));
    }

    @Override
    public List<FinanceOperation> getOperationsByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return Collections.emptyList();
        }

        List<FinanceOperation> result = new ArrayList<>();
        for (FinanceOperation operation : operations) {
            if (operation == null || operation.getDateTime() == null) {
                continue;
            }

            LocalDateTime operationDate = operation.getDateTime();
            // Check if operation date is within range (inclusive)
            if (!operationDate.isBefore(start) && !operationDate.isAfter(end)) {
                result.add(operation);
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public BigDecimal calculateTotalIncome() {
        return calculateTotalIncome(null, null);
    }

    @Override
    public BigDecimal calculateTotalIncome(LocalDateTime start, LocalDateTime end) {
        BigDecimal total = BigDecimal.ZERO;

        for (FinanceOperation operation : operations) {
            if (operation == null || operation.getAmount() == null) {
                continue;
            }

            // Filter by date range if provided
            if (start != null && end != null) {
                LocalDateTime operationDate = operation.getDateTime();
                if (operationDate == null || operationDate.isBefore(start) || operationDate.isAfter(end)) {
                    continue;
                }
            }

            if (operation.getType() == FinanceOperationType.INCOME) {
                total = total.add(operation.getAmount());
            }
        }

        return total;
    }

    @Override
    public BigDecimal calculateTotalExpenses() {
        return calculateTotalExpenses(null, null);
    }

    @Override
    public BigDecimal calculateTotalExpenses(LocalDateTime start, LocalDateTime end) {
        BigDecimal total = BigDecimal.ZERO;

        for (FinanceOperation operation : operations) {
            if (operation == null || operation.getAmount() == null) {
                continue;
            }

            // Filter by date range if provided
            if (start != null && end != null) {
                LocalDateTime operationDate = operation.getDateTime();
                if (operationDate == null || operationDate.isBefore(start) || operationDate.isAfter(end)) {
                    continue;
                }
            }

            if (operation.getType() == FinanceOperationType.EXPENSE) {
                total = total.add(operation.getAmount());
            }
        }

        return total;
    }

    @Override
    public BigDecimal calculateNetProfit() {
        return calculateNetProfit(null, null);
    }

    @Override
    public BigDecimal calculateNetProfit(LocalDateTime start, LocalDateTime end) {
        BigDecimal income = calculateTotalIncome(start, end);
        BigDecimal expenses = calculateTotalExpenses(start, end);
        return income.subtract(expenses);
    }

    @Override
    public FinanceSummary getSummary() {
        BigDecimal totalIncome = calculateTotalIncome();
        BigDecimal totalExpenses = calculateTotalExpenses();
        return new FinanceSummary(totalIncome, totalExpenses);
    }

    @Override
    public void recordPaidOrderProfit(Order order, BigDecimal orderRevenue) {
        if (order == null || order.getId() == null || orderRevenue == null) {
            return;
        }
        if (recordedOrderIds.contains(order.getId())) {
            return; // Already recorded, prevent double-counting
        }
        BigDecimal orderCost = order.getTotalCost();
        if (orderCost == null) {
            orderCost = BigDecimal.ZERO;
        }
        BigDecimal orderProfit = orderRevenue.subtract(orderCost);

        totalRevenueFromOrders = totalRevenueFromOrders.add(orderRevenue);
        totalCostFromOrders = totalCostFromOrders.add(orderCost);
        totalProfitFromOrders = totalProfitFromOrders.add(orderProfit);
        recordedOrderIds.add(order.getId());

        // Per-dish stats
        if (order.getItems() != null && techCardService != null) {
            for (OrderItem item : order.getItems()) {
                if (item == null || item.getDish() == null || item.getDish().getId() == null || item.getQuantity() <= 0) {
                    continue;
                }
                Dish dish = item.getDish();
                Long dishId = dish.getId();
                int qty = item.getQuantity();
                BigDecimal dishCost = techCardService.calculateDishCost(dishId);
                if (dishCost == null) dishCost = BigDecimal.ZERO;
                BigDecimal itemCost = dishCost.multiply(BigDecimal.valueOf(qty));
                BigDecimal itemRevenue = dish.getSalePrice() != null
                        ? dish.getSalePrice().multiply(BigDecimal.valueOf(qty))
                        : BigDecimal.ZERO;
                BigDecimal itemProfit = itemRevenue.subtract(itemCost);

                DishStatsMutable stats = dishStatsMap.get(dishId);
                if (stats == null) {
                    stats = new DishStatsMutable(dishId, dish.getName());
                    dishStatsMap.put(dishId, stats);
                }
                stats.timesSold += qty;
                stats.totalRevenue = stats.totalRevenue.add(itemRevenue);
                stats.totalCost = stats.totalCost.add(itemCost);
                stats.totalProfit = stats.totalProfit.add(itemProfit);
            }
        }

        // Profit per waiter
        Long waiterId = order.getWaiterId();
        if (waiterId != null) {
            BigDecimal current = profitByWaiter.getOrDefault(waiterId, BigDecimal.ZERO);
            profitByWaiter.put(waiterId, current.add(orderProfit));
        }
    }

    @Override
    public BigDecimal getTotalRevenueFromOrders() {
        return totalRevenueFromOrders;
    }

    @Override
    public BigDecimal getTotalCostFromOrders() {
        return totalCostFromOrders;
    }

    @Override
    public BigDecimal getTotalProfitFromOrders() {
        return totalProfitFromOrders;
    }

    @Override
    public Map<Long, DishProfitStats> getAllDishStats() {
        Map<Long, DishProfitStats> result = new HashMap<>();
        for (Map.Entry<Long, DishStatsMutable> e : dishStatsMap.entrySet()) {
            DishStatsMutable m = e.getValue();
            result.put(e.getKey(), new DishProfitStats(m.dishId, m.dishName, m.timesSold, m.totalRevenue, m.totalCost, m.totalProfit));
        }
        return result;
    }

    @Override
    public Map<Long, BigDecimal> getProfitByWaiter() {
        return new HashMap<>(profitByWaiter);
    }

    private static class DishStatsMutable {
        final Long dishId;
        final String dishName;
        int timesSold;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        DishStatsMutable(Long dishId, String dishName) {
            this.dishId = dishId;
            this.dishName = dishName != null ? dishName : "";
        }
    }
}
