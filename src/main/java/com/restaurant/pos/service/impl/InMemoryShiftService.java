package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.PaymentMethod;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.model.ShiftModel;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.ShiftService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of ShiftService.
 * Manages waiter shifts with order attachment and revenue calculation.
 * Ensures only one active shift per waiter.
 */
public class InMemoryShiftService implements ShiftService {

    private final List<ShiftModel> shifts = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final OrderPricingService orderPricingService;

    public InMemoryShiftService() {
        this.orderPricingService = null;
    }

    public InMemoryShiftService(OrderPricingService orderPricingService) {
        this.orderPricingService = orderPricingService;
    }

    @Override
    public ShiftModel openShift(Long waiterId) {
        if (waiterId == null) {
            return null;
        }

        // Check if waiter already has an active shift
        ShiftModel activeShift = getActiveShift(waiterId);
        if (activeShift != null) {
            return activeShift;
        }

        // Create new shift
        ShiftModel shift = new ShiftModel();
        shift.setId(idGenerator.getAndIncrement());
        shift.setWaiterId(waiterId);
        shift.setStartTime(LocalDateTime.now());
        shift.setOrders(new ArrayList<>());
        shift.setTotalRevenue(BigDecimal.ZERO);
        shift.setTotalProfit(BigDecimal.ZERO);
        shift.setClosed(false);
        shifts.add(shift);

        return shift;
    }

    @Override
    public ShiftModel closeShift(Long waiterId) {
        if (waiterId == null) {
            return null;
        }

        ShiftModel activeShift = getActiveShift(waiterId);
        if (activeShift == null) {
            return null;
        }

        // Calculate final revenue before closing
        BigDecimal revenue = calculateShiftRevenue(activeShift.getId());
        activeShift.setTotalRevenue(revenue);

        // Close the shift
        activeShift.setEndTime(LocalDateTime.now());

        return activeShift;
    }

    @Override
    public ShiftModel getActiveShift(Long waiterId) {
        ShiftModel global = getActiveRestaurantShift();
        if (global != null) return global;
        if (waiterId == null) return null;
        for (ShiftModel shift : shifts) {
            if (shift.getWaiterId() != null && shift.getWaiterId().equals(waiterId)
                    && shift.getEndTime() == null && !shift.isClosed()) {
                return shift;
            }
        }
        return null;
    }

    @Override
    public void ensureActiveShift() {
        if (getActiveRestaurantShift() != null) return;
        ShiftModel shift = new ShiftModel();
        shift.setId(idGenerator.getAndIncrement());
        shift.setStartTime(LocalDateTime.now());
        shift.setTotalRevenue(BigDecimal.ZERO);
        shift.setTotalProfit(BigDecimal.ZERO);
        shift.setClosed(false);
        shifts.add(shift);
    }

    @Override
    public ShiftModel getActiveRestaurantShift() {
        for (ShiftModel shift : shifts) {
            if (shift.getEndTime() == null && !shift.isClosed()) {
                return shift;
            }
        }
        return null;
    }

    @Override
    public ShiftSummary closeRestaurantShift() {
        ShiftModel active = getActiveRestaurantShift();
        if (active == null) {
            return new ShiftSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        BigDecimal revenue = calculateShiftRevenue(active.getId());
        BigDecimal cost = calculateShiftCost(active.getId());
        BigDecimal profit = revenue.subtract(cost);
        BigDecimal cash = BigDecimal.ZERO;
        BigDecimal card = BigDecimal.ZERO;
        int count = 0;
        if (active.getOrders() != null) {
            for (Order o : active.getOrders()) {
                if (o != null && o.isPaid()) {
                    count++;
                    if (o.getPaymentMethod() == PaymentMethod.CASH && o.getAmountPaid() != null) {
                        cash = cash.add(o.getAmountPaid());
                    } else if (o.getPaymentMethod() == PaymentMethod.CARD) {
                        BigDecimal amt = orderPricingService != null ? orderPricingService.calculateTotal(o) : BigDecimal.ZERO;
                        if (amt != null) card = card.add(amt);
                    }
                }
            }
        }
        active.setTotalRevenue(revenue);
        active.setTotalProfit(profit);
        active.setEndTime(LocalDateTime.now());
        active.setClosed(true);
        ensureActiveShift();
        return new ShiftSummary(revenue, cost, profit, count, cash, card);
    }

    private BigDecimal calculateShiftCost(Long shiftId) {
        ShiftModel shift = getShiftById(shiftId);
        if (shift == null || shift.getOrders() == null) return BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        for (Order o : shift.getOrders()) {
            if (o != null && o.getTotalCost() != null) cost = cost.add(o.getTotalCost());
        }
        return cost;
    }

    @Override
    public boolean isShiftOpen(Long waiterId) {
        return getActiveShift(waiterId) != null;
    }

    @Override
    public boolean attachOrderToActiveShift(Order order) {
        if (order == null) return false;
        ShiftModel activeShift = getActiveRestaurantShift();
        if (activeShift == null) {
            return false;
        }

        // Set order's shiftId
        order.setShiftId(activeShift.getId());

        // Add order to shift's orders list
        List<Order> orders = activeShift.getOrders();
        if (orders == null) {
            orders = new ArrayList<>();
            activeShift.setOrders(orders);
        }
        orders.add(order);
        BigDecimal revenue = calculateShiftRevenue(activeShift.getId());
        BigDecimal cost = calculateShiftCost(activeShift.getId());
        activeShift.setTotalRevenue(revenue);
        activeShift.setTotalProfit(revenue.subtract(cost));
        return true;
    }

    @Override
    public BigDecimal calculateShiftRevenue(Long shiftId) {
        if (shiftId == null) {
            return BigDecimal.ZERO;
        }

        ShiftModel shift = getShiftById(shiftId);
        if (shift == null || shift.getOrders() == null) {
            return BigDecimal.ZERO;
        }

        if (orderPricingService == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order order : shift.getOrders()) {
            if (order != null) {
                BigDecimal orderRevenue = orderPricingService.calculateTotal(order);
                if (orderRevenue != null) {
                    totalRevenue = totalRevenue.add(orderRevenue);
                }
            }
        }

        return totalRevenue;
    }

    @Override
    public ShiftModel getShiftById(Long shiftId) {
        if (shiftId == null) {
            return null;
        }

        for (ShiftModel shift : shifts) {
            if (shift.getId() != null && shift.getId().equals(shiftId)) {
                return shift;
            }
        }
        return null;
    }
}
