package com.restaurant.pos.service.impl;

import com.restaurant.pos.model.AverageCheckDTO;
import com.restaurant.pos.model.InMemoryOrderStore;
import com.restaurant.pos.model.OrdersCountDTO;
import com.restaurant.pos.model.TotalRevenueDTO;
import com.restaurant.pos.model.UiOrder;
import com.restaurant.pos.service.DashboardService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * DashboardService implementation that calculates KPIs from InMemoryOrderStore.
 */
public class InMemoryDashboardService implements DashboardService {

    private final InMemoryOrderStore orderStore;

    public InMemoryDashboardService(InMemoryOrderStore orderStore) {
        this.orderStore = orderStore;
    }

    @Override
    public TotalRevenueDTO getTotalRevenue() {
        List<UiOrder> orders = orderStore != null ? orderStore.getOrders() : List.of();
        BigDecimal total = BigDecimal.ZERO;
        for (UiOrder order : orders) {
            if (order != null && order.getTotalAmount() != null) {
                total = total.add(order.getTotalAmount());
            }
        }
        return new TotalRevenueDTO(total);
    }

    @Override
    public OrdersCountDTO getOrdersCount() {
        List<UiOrder> orders = orderStore != null ? orderStore.getOrders() : List.of();
        return new OrdersCountDTO(orders.size());
    }

    @Override
    public AverageCheckDTO getAverageCheck() {
        List<UiOrder> orders = orderStore != null ? orderStore.getOrders() : List.of();
        if (orders.isEmpty()) {
            return new AverageCheckDTO(BigDecimal.ZERO);
        }

        BigDecimal totalRevenue = getTotalRevenue().getTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        int count = orders.size();
        if (count == 0 || totalRevenue.signum() == 0) {
            return new AverageCheckDTO(BigDecimal.ZERO);
        }

        BigDecimal avg = totalRevenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        return new AverageCheckDTO(avg);
    }

    @Override
    public TotalRevenueDTO getRevenueByShift(Long shiftId) {
        if (shiftId == null) {
            return new TotalRevenueDTO(BigDecimal.ZERO);
        }
        List<UiOrder> orders = orderStore != null ? orderStore.getOrders() : List.of();
        BigDecimal total = BigDecimal.ZERO;
        for (UiOrder order : orders) {
            if (order != null 
                    && order.getShiftId() != null 
                    && order.getShiftId().equals(shiftId)
                    && order.getTotalAmount() != null) {
                total = total.add(order.getTotalAmount());
            }
        }
        return new TotalRevenueDTO(total);
    }

    @Override
    public OrdersCountDTO getOrdersCountByShift(Long shiftId) {
        if (shiftId == null) {
            return new OrdersCountDTO(0);
        }
        List<UiOrder> orders = orderStore != null ? orderStore.getOrders() : List.of();
        int count = 0;
        for (UiOrder order : orders) {
            if (order != null 
                    && order.getShiftId() != null 
                    && order.getShiftId().equals(shiftId)) {
                count++;
            }
        }
        return new OrdersCountDTO(count);
    }
}

