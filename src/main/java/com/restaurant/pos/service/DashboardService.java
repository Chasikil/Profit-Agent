package com.restaurant.pos.service;

import com.restaurant.pos.model.AverageCheckDTO;
import com.restaurant.pos.model.OrdersCountDTO;
import com.restaurant.pos.model.TotalRevenueDTO;

/**
 * Service for calculating dashboard KPIs from in-memory orders store.
 * No UI dependencies.
 */
public interface DashboardService {

    TotalRevenueDTO getTotalRevenue();

    OrdersCountDTO getOrdersCount();

    AverageCheckDTO getAverageCheck();

    /**
     * Get total revenue for a specific shift.
     * @param shiftId shift ID
     * @return TotalRevenueDTO with revenue for the shift
     */
    TotalRevenueDTO getRevenueByShift(Long shiftId);

    /**
     * Get orders count for a specific shift.
     * @param shiftId shift ID
     * @return OrdersCountDTO with count of orders for the shift
     */
    OrdersCountDTO getOrdersCountByShift(Long shiftId);
}

