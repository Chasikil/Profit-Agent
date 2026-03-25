package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.service.CostCalculationService;
import com.restaurant.pos.service.KPIService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class SimpleKPIService implements KPIService {

    private final OrderService orderService;
    private final OrderPricingService orderPricingService;
    private final CostCalculationService costCalculationService;

    public SimpleKPIService(OrderService orderService,
                           OrderPricingService orderPricingService,
                           CostCalculationService costCalculationService) {
        this.orderService = orderService;
        this.orderPricingService = orderPricingService;
        this.costCalculationService = costCalculationService;
    }

    @Override
    public BigDecimal getAverageMargin(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderService.getOrdersByPeriod(start, end);
        if (orders == null || orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.PAID) {
                continue;
            }
            BigDecimal revenue = orderPricingService.calculateTotal(order);
            BigDecimal cost = costCalculationService.calculateOrderCost(order);
            totalRevenue = totalRevenue.add(revenue);
            totalCost = totalCost.add(cost);
        }
        if (totalRevenue.signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal profit = totalRevenue.subtract(totalCost);
        return profit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public BigDecimal getFoodCostPercent(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderService.getOrdersByPeriod(start, end);
        if (orders == null || orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.PAID) {
                continue;
            }
            BigDecimal revenue = orderPricingService.calculateTotal(order);
            BigDecimal cost = costCalculationService.calculateOrderCost(order);
            totalRevenue = totalRevenue.add(revenue);
            totalCost = totalCost.add(cost);
        }
        if (totalRevenue.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return totalCost.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public BigDecimal getRevenuePerOrder(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderService.getOrdersByPeriod(start, end);
        if (orders == null || orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int paidOrdersCount = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.PAID) {
                continue;
            }
            paidOrdersCount++;
            BigDecimal revenue = orderPricingService.calculateTotal(order);
            totalRevenue = totalRevenue.add(revenue);
        }
        if (paidOrdersCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(paidOrdersCount), 2, RoundingMode.HALF_UP);
    }
}
