package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.FinanceOperation;
import com.restaurant.pos.domain.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing finance operations (income and expenses).
 * Tracks financial transactions for the restaurant.
 */
public interface FinanceService {

    /**
     * Add a finance operation.
     * 
     * @param operation finance operation to add
     */
    void addOperation(FinanceOperation operation);

    /**
     * Delete a finance operation by ID.
     * 
     * @param operationId ID of the operation to delete
     * @return true if operation was deleted, false if not found
     */
    boolean deleteOperation(Long operationId);

    /**
     * Get all finance operations.
     * 
     * @return list of all finance operations
     */
    List<FinanceOperation> getAll();

    /**
     * Get finance operations within a date range.
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return list of finance operations within the date range
     */
    List<FinanceOperation> getOperationsByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Calculate total income from all operations.
     * 
     * @return total income amount
     */
    BigDecimal calculateTotalIncome();

    /**
     * Calculate total income within a date range.
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return total income amount for the date range
     */
    BigDecimal calculateTotalIncome(LocalDateTime start, LocalDateTime end);

    /**
     * Calculate total expenses from all operations.
     * 
     * @return total expenses amount
     */
    BigDecimal calculateTotalExpenses();

    /**
     * Calculate total expenses within a date range.
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return total expenses amount for the date range
     */
    BigDecimal calculateTotalExpenses(LocalDateTime start, LocalDateTime end);

    /**
     * Calculate net profit (income - expenses) from all operations.
     * 
     * @return net profit amount
     */
    BigDecimal calculateNetProfit();

    /**
     * Calculate net profit (income - expenses) within a date range.
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return net profit amount for the date range
     */
    BigDecimal calculateNetProfit(LocalDateTime start, LocalDateTime end);

    /**
     * Get financial summary.
     * 
     * @return summary with total income and total expenses
     */
    FinanceSummary getSummary();

    // ---------- Profit analytics (PAID orders only) ----------

    /**
     * Record a paid order for profit analytics.
     * Updates totalRevenue, totalCost, totalProfit, per-dish stats, profit per waiter.
     * Must be called exactly once per paid order to avoid double-counting.
     *
     * @param order paid order (must have totalCost, totalProfit set; status PAID)
     * @param orderRevenue total revenue for this order
     */
    void recordPaidOrderProfit(Order order, BigDecimal orderRevenue);

    /** Total revenue from all paid orders. */
    BigDecimal getTotalRevenueFromOrders();

    /** Total cost (ingredients) from all paid orders. */
    BigDecimal getTotalCostFromOrders();

    /** Total profit from all paid orders (revenue - cost). */
    BigDecimal getTotalProfitFromOrders();

    /** Per-dish stats: dishId -> DishProfitStats. */
    Map<Long, DishProfitStats> getAllDishStats();

    /** Profit per waiter: waiterId -> total profit. */
    Map<Long, BigDecimal> getProfitByWaiter();

    /**
     * Stats for a single dish (from paid orders only).
     */
    class DishProfitStats {
        private final Long dishId;
        private final String dishName;
        private final int timesSold;
        private final BigDecimal totalRevenue;
        private final BigDecimal totalCost;
        private final BigDecimal totalProfit;

        public DishProfitStats(Long dishId, String dishName, int timesSold,
                              BigDecimal totalRevenue, BigDecimal totalCost, BigDecimal totalProfit) {
            this.dishId = dishId;
            this.dishName = dishName != null ? dishName : "";
            this.timesSold = timesSold;
            this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
            this.totalCost = totalCost != null ? totalCost : BigDecimal.ZERO;
            this.totalProfit = totalProfit != null ? totalProfit : BigDecimal.ZERO;
        }

        public Long getDishId() { return dishId; }
        public String getDishName() { return dishName; }
        public int getTimesSold() { return timesSold; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public BigDecimal getTotalCost() { return totalCost; }
        public BigDecimal getTotalProfit() { return totalProfit; }
    }

    /**
     * Summary of finance operations.
     */
    class FinanceSummary {
        private final BigDecimal totalIncome;
        private final BigDecimal totalExpenses;

        public FinanceSummary(BigDecimal totalIncome, BigDecimal totalExpenses) {
            this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
            this.totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        }

        public BigDecimal getTotalIncome() {
            return totalIncome;
        }

        public BigDecimal getTotalExpenses() {
            return totalExpenses;
        }

        public BigDecimal getNetProfit() {
            return totalIncome.subtract(totalExpenses);
        }
    }
}
