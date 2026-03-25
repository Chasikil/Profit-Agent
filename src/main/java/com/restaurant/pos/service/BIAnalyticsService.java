package com.restaurant.pos.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Business intelligence analytics: period-based reports, margin analysis, waiter KPI.
 * All metrics computed from PAID orders only. Uses database/order data.
 */
public interface BIAnalyticsService {

    /**
     * Dish statistics with margin for a period.
     */
    record DishMarginStats(
            Long dishId,
            String dishName,
            int timesSold,
            BigDecimal revenue,
            BigDecimal totalCost,
            BigDecimal grossProfit,
            BigDecimal marginPercent
    ) {}

    /**
     * Waiter KPI for a period.
     */
    record WaiterKPI(
            Long waiterId,
            String waiterName,
            int ordersHandled,
            BigDecimal totalRevenue,
            BigDecimal totalProfit,
            BigDecimal averageOrderValue,
            BigDecimal averageProfitPerOrder
    ) {}

    /**
     * Period summary for analytics.
     */
    record PeriodSummary(
            BigDecimal revenue,
            BigDecimal cost,
            BigDecimal profit,
            BigDecimal marginPercent,
            int orderCount
    ) {}

    /**
     * Management overview summary.
     */
    record ManagementSummary(
            BigDecimal revenue,
            BigDecimal profit,
            String bestSellingDish,
            String mostProfitableDish,
            String weakestMarginDish,
            String bestWaiterByProfit,
            BigDecimal totalIngredientsConsumed,
            BigDecimal averageOrderValue
    ) {}

    /** Get dish margin stats for period (PAID orders only). */
    List<DishMarginStats> getDishMarginStats(LocalDate start, LocalDate end);

    /** Get waiter KPI for period (PAID orders only). */
    List<WaiterKPI> getWaiterKPI(LocalDate start, LocalDate end);

    /** Get period summary (revenue, cost, profit, margin). */
    PeriodSummary getPeriodSummary(LocalDate start, LocalDate end);

    /** Get management summary for dashboard. */
    ManagementSummary getManagementSummary(LocalDate start, LocalDate end);

    /** Get profit by waiter for period (for backward compatibility). */
    Map<Long, BigDecimal> getProfitByWaiter(LocalDate start, LocalDate end);
}
