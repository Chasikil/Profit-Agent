package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.model.ShiftModel;

/**
 * Service for managing waiter shifts.
 * Responsibilities:
 * - Open shift
 * - Close shift
 * - Attach orders to active shift
 * - Calculate shift revenue
 * 
 * Only one active shift per waiter.
 */
public interface ShiftService {

    /**
     * Open a new shift for waiter.
     * If waiter already has an active shift, returns the existing shift.
     * 
     * @param waiterId waiter ID
     * @return created or existing active shift, or null if failed
     */
    ShiftModel openShift(Long waiterId);

    /**
     * Close active shift for waiter.
     * 
     * @param waiterId waiter ID
     * @return closed shift, or null if no active shift found
     */
    ShiftModel closeShift(Long waiterId);

    /**
     * Get active shift for waiter.
     * 
     * @param waiterId waiter ID
     * @return active shift or null if no active shift
     */
    ShiftModel getActiveShift(Long waiterId);

    /**
     * Check if waiter has an active shift.
     * 
     * @param waiterId waiter ID
     * @return true if waiter has an active shift
     */
    boolean isShiftOpen(Long waiterId);

    /**
     * Attach order to active shift.
     * Order's shiftId is set to the active shift's ID.
     * 
     * @param order order to attach
     * @return true if attached successfully, false if no active shift
     */
    boolean attachOrderToActiveShift(Order order);

    /**
     * Calculate total revenue for shift.
     * Sums up revenue from all orders in the shift.
     * 
     * @param shiftId shift ID
     * @return total revenue for the shift
     */
    java.math.BigDecimal calculateShiftRevenue(Long shiftId);

    /**
     * Get shift by ID.
     * 
     * @param shiftId shift ID
     * @return shift or null if not found
     */
    ShiftModel getShiftById(Long shiftId);

    /**
     * Ensure an open shift exists. If none, create one.
     * Call on application start.
     */
    void ensureActiveShift();

    /**
     * Get the single active restaurant shift (global).
     *
     * @return active shift or null
     */
    ShiftModel getActiveRestaurantShift();

    /**
     * Close the active restaurant shift, generate summary, create new shift.
     *
     * @return summary of closed shift (revenue, profit, order count, cash/card totals)
     */
    ShiftSummary closeRestaurantShift();

    /**
     * Summary of closed shift.
     */
    class ShiftSummary {
        public final java.math.BigDecimal totalRevenue;
        public final java.math.BigDecimal totalCost;
        public final java.math.BigDecimal totalProfit;
        public final int orderCount;
        public final java.math.BigDecimal cashTotal;
        public final java.math.BigDecimal cardTotal;

        public ShiftSummary(java.math.BigDecimal totalRevenue, java.math.BigDecimal totalCost, java.math.BigDecimal totalProfit,
                           int orderCount, java.math.BigDecimal cashTotal, java.math.BigDecimal cardTotal) {
            this.totalRevenue = totalRevenue != null ? totalRevenue : java.math.BigDecimal.ZERO;
            this.totalCost = totalCost != null ? totalCost : java.math.BigDecimal.ZERO;
            this.totalProfit = totalProfit != null ? totalProfit : java.math.BigDecimal.ZERO;
            this.orderCount = orderCount;
            this.cashTotal = cashTotal != null ? cashTotal : java.math.BigDecimal.ZERO;
            this.cardTotal = cardTotal != null ? cardTotal : java.math.BigDecimal.ZERO;
        }
    }
}
