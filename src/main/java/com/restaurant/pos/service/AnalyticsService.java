package com.restaurant.pos.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service for analytics and reporting.
 * Provides various analytics metrics for the restaurant.
 */
public interface AnalyticsService {

    /**
     * Get orders count per day for a date range.
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return map of date to order count
     */
    Map<LocalDate, Integer> getOrdersCountPerDay(LocalDate start, LocalDate end);

    /**
     * Get revenue per day for a date range.
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return map of date to revenue amount
     */
    Map<LocalDate, BigDecimal> getRevenuePerDay(LocalDate start, LocalDate end);

    /**
     * Get revenue per category for a date range.
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return map of category name to revenue amount
     */
    Map<String, BigDecimal> getRevenuePerCategory(LocalDate start, LocalDate end);

    /**
     * Get top selling menu items for a date range.
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @param limit maximum number of items to return
     * @return list of menu item sales data, sorted by quantity (descending)
     */
    List<MenuItemSales> getTopSellingMenuItems(LocalDate start, LocalDate end, int limit);

    /**
     * Get expenses per category for a date range.
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return map of category name to expense amount
     */
    Map<String, BigDecimal> getExpensesPerCategory(LocalDate start, LocalDate end);

    /**
     * Get profit per day for a date range.
     * Profit = Revenue - Expenses
     * 
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return map of date to profit amount
     */
    Map<LocalDate, BigDecimal> getProfitPerDay(LocalDate start, LocalDate end);

    /**
     * Data class for menu item sales statistics.
     */
    class MenuItemSales {
        private final Long dishId;
        private final String dishName;
        private final int totalQuantity;
        private final BigDecimal totalRevenue;

        public MenuItemSales(Long dishId, String dishName, int totalQuantity, BigDecimal totalRevenue) {
            this.dishId = dishId;
            this.dishName = dishName;
            this.totalQuantity = totalQuantity;
            this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        }

        public Long getDishId() {
            return dishId;
        }

        public String getDishName() {
            return dishName;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }
    }
}
