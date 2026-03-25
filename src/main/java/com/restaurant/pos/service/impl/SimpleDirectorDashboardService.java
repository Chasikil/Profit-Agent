package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.model.DashboardMetrics;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.service.CostCalculationService;
import com.restaurant.pos.service.DirectorDashboardService;
import com.restaurant.pos.service.KPIService;
import com.restaurant.pos.service.NetProfitService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;
import com.restaurant.pos.service.SalaryReportService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SimpleDirectorDashboardService implements DirectorDashboardService {

    private final OrderService orderService;
    private final OrderPricingService orderPricingService;
    private final CostCalculationService costCalculationService;
    private final SalaryReportService salaryReportService;
    private final NetProfitService netProfitService;
    private final KPIService kpiService;

    public SimpleDirectorDashboardService(OrderService orderService,
                                          OrderPricingService orderPricingService,
                                          CostCalculationService costCalculationService,
                                          SalaryReportService salaryReportService,
                                          NetProfitService netProfitService,
                                          KPIService kpiService) {
        this.orderService = orderService;
        this.orderPricingService = orderPricingService;
        this.costCalculationService = costCalculationService;
        this.salaryReportService = salaryReportService;
        this.netProfitService = netProfitService;
        this.kpiService = kpiService;
    }

    @Override
    public DashboardMetrics getDashboardMetrics(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return createEmptyMetrics();
        }
        DashboardMetrics metrics = new DashboardMetrics();
        List<Order> orders = orderService != null ? orderService.getOrdersByPeriod(start, end) : null;
        BigDecimal totalRevenue = calculateTotalRevenue(orders);
        BigDecimal totalCost = calculateTotalCost(orders);
        BigDecimal salaryExpenses = salaryReportService != null ? salaryReportService.getSalaryExpenses(start, end) : BigDecimal.ZERO;
        BigDecimal netProfit = netProfitService != null ? netProfitService.calculateNetProfit(start, end) : BigDecimal.ZERO;
        BigDecimal averageMargin = kpiService != null ? kpiService.getAverageMargin(start, end) : BigDecimal.ZERO;
        BigDecimal foodCostPercent = kpiService != null ? kpiService.getFoodCostPercent(start, end) : BigDecimal.ZERO;
        metrics.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        metrics.setTotalCost(totalCost != null ? totalCost : BigDecimal.ZERO);
        metrics.setSalaryExpenses(salaryExpenses != null ? salaryExpenses : BigDecimal.ZERO);
        metrics.setNetProfit(netProfit != null ? netProfit : BigDecimal.ZERO);
        metrics.setAverageMargin(averageMargin != null ? averageMargin : BigDecimal.ZERO);
        metrics.setFoodCostPercent(foodCostPercent != null ? foodCostPercent : BigDecimal.ZERO);
        return metrics;
    }

    private BigDecimal calculateTotalRevenue(List<Order> orders) {
        BigDecimal total = BigDecimal.ZERO;
        if (orders == null || orderPricingService == null) {
            return total;
        }
        for (Order order : orders) {
            if (order != null && order.getStatus() == OrderStatus.PAID) {
                BigDecimal revenue = orderPricingService.calculateTotal(order);
                if (revenue != null) {
                    total = total.add(revenue);
                }
            }
        }
        return total;
    }

    private BigDecimal calculateTotalCost(List<Order> orders) {
        BigDecimal total = BigDecimal.ZERO;
        if (orders == null || costCalculationService == null) {
            return total;
        }
        for (Order order : orders) {
            if (order != null && order.getStatus() == OrderStatus.PAID) {
                BigDecimal cost = costCalculationService.calculateOrderCost(order);
                if (cost != null) {
                    total = total.add(cost);
                }
            }
        }
        return total;
    }

    private DashboardMetrics createEmptyMetrics() {
        DashboardMetrics metrics = new DashboardMetrics();
        metrics.setTotalRevenue(BigDecimal.ZERO);
        metrics.setTotalCost(BigDecimal.ZERO);
        metrics.setSalaryExpenses(BigDecimal.ZERO);
        metrics.setNetProfit(BigDecimal.ZERO);
        metrics.setAverageMargin(BigDecimal.ZERO);
        metrics.setFoodCostPercent(BigDecimal.ZERO);
        return metrics;
    }
}
