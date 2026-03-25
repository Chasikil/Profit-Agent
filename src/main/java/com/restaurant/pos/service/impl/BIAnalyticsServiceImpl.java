package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.model.Dish;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.domain.model.OrderItem;
import com.restaurant.pos.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Period-based BI analytics. Aggregates from PAID orders only.
 */
public class BIAnalyticsServiceImpl implements BIAnalyticsService {

    private final OrderService orderService;
    private final OrderPricingService orderPricingService;
    private final TechCardService techCardService;
    private final EmployeeStorageService employeeStorageService;

    public BIAnalyticsServiceImpl(OrderService orderService,
                                  OrderPricingService orderPricingService,
                                  TechCardService techCardService,
                                  EmployeeStorageService employeeStorageService) {
        this.orderService = orderService;
        this.orderPricingService = orderPricingService;
        this.techCardService = techCardService;
        this.employeeStorageService = employeeStorageService;
    }

    private List<Order> getPaidOrdersInPeriod(LocalDate start, LocalDate end) {
        if (orderService == null || start == null || end == null || end.isBefore(start)) {
            return Collections.emptyList();
        }
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);
        List<Order> orders = orderService.getOrdersByPeriod(startDt, endDt);
        return orders.stream()
                .filter(o -> o != null && o.getStatus() == OrderStatus.PAID)
                .collect(Collectors.toList());
    }

    @Override
    public List<DishMarginStats> getDishMarginStats(LocalDate start, LocalDate end) {
        List<Order> orders = getPaidOrdersInPeriod(start, end);
        Map<Long, DishAgg> agg = new HashMap<>();

        for (Order order : orders) {
            if (order.getItems() == null) continue;
            for (OrderItem item : order.getItems()) {
                if (item == null || item.getDish() == null) continue;
                Dish dish = item.getDish();
                Long dishId = dish.getId();
                if (dishId == null) continue;

                int qty = item.getQuantity();
                if (qty <= 0) continue;

                BigDecimal price = dish.getSalePrice() != null ? dish.getSalePrice() : BigDecimal.ZERO;
                BigDecimal revenue = price.multiply(BigDecimal.valueOf(qty));
                BigDecimal cost = BigDecimal.ZERO;
                if (techCardService != null) {
                    BigDecimal dishCost = techCardService.calculateDishCost(dishId);
                    if (dishCost != null) cost = dishCost.multiply(BigDecimal.valueOf(qty));
                }
                BigDecimal profit = revenue.subtract(cost);

                DishAgg d = agg.computeIfAbsent(dishId, k -> new DishAgg(dish.getName()));
                d.timesSold += qty;
                d.revenue = d.revenue.add(revenue);
                d.cost = d.cost.add(cost);
                d.profit = d.profit.add(profit);
            }
        }

        List<DishMarginStats> result = new ArrayList<>();
        for (Map.Entry<Long, DishAgg> e : agg.entrySet()) {
            DishAgg d = e.getValue();
            BigDecimal marginPct = BigDecimal.ZERO;
            if (d.revenue.signum() > 0) {
                marginPct = d.profit.multiply(BigDecimal.valueOf(100))
                        .divide(d.revenue, 2, RoundingMode.HALF_UP);
            }
            result.add(new DishMarginStats(e.getKey(), d.name, d.timesSold,
                    d.revenue, d.cost, d.profit, marginPct));
        }
        result.sort(Comparator.comparing(DishMarginStats::revenue).reversed());
        return result;
    }

    @Override
    public List<WaiterKPI> getWaiterKPI(LocalDate start, LocalDate end) {
        List<Order> orders = getPaidOrdersInPeriod(start, end);
        Map<Long, WaiterAgg> agg = new HashMap<>();

        for (Order order : orders) {
            Long waiterId = order.getWaiterId();
            if (waiterId == null) continue;

            BigDecimal revenue = orderPricingService != null ? orderPricingService.calculateTotal(order) : BigDecimal.ZERO;
            if (revenue == null) revenue = BigDecimal.ZERO;
            BigDecimal cost = order.getTotalCost() != null ? order.getTotalCost() : BigDecimal.ZERO;
            BigDecimal profit = revenue.subtract(cost);

            WaiterAgg w = agg.computeIfAbsent(waiterId, k -> new WaiterAgg(getWaiterName(waiterId)));
            w.ordersHandled++;
            w.totalRevenue = w.totalRevenue.add(revenue);
            w.totalProfit = w.totalProfit.add(profit);
        }

        List<WaiterKPI> result = new ArrayList<>();
        for (Map.Entry<Long, WaiterAgg> e : agg.entrySet()) {
            WaiterAgg w = e.getValue();
            BigDecimal avgOrder = w.ordersHandled > 0
                    ? w.totalRevenue.divide(BigDecimal.valueOf(w.ordersHandled), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal avgProfit = w.ordersHandled > 0
                    ? w.totalProfit.divide(BigDecimal.valueOf(w.ordersHandled), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            result.add(new WaiterKPI(e.getKey(), w.name, w.ordersHandled,
                    w.totalRevenue, w.totalProfit, avgOrder, avgProfit));
        }
        result.sort(Comparator.comparing(WaiterKPI::totalProfit).reversed());
        return result;
    }

    @Override
    public PeriodSummary getPeriodSummary(LocalDate start, LocalDate end) {
        List<Order> orders = getPaidOrdersInPeriod(start, end);
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        for (Order o : orders) {
            BigDecimal r = orderPricingService != null ? orderPricingService.calculateTotal(o) : BigDecimal.ZERO;
            if (r != null) revenue = revenue.add(r);
            BigDecimal c = o.getTotalCost() != null ? o.getTotalCost() : BigDecimal.ZERO;
            cost = cost.add(c);
        }
        BigDecimal profit = revenue.subtract(cost);
        BigDecimal marginPct = revenue.signum() > 0
                ? profit.multiply(BigDecimal.valueOf(100)).divide(revenue, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new PeriodSummary(revenue, cost, profit, marginPct, orders.size());
    }

    @Override
    public ManagementSummary getManagementSummary(LocalDate start, LocalDate end) {
        List<DishMarginStats> dishStats = getDishMarginStats(start, end);
        List<WaiterKPI> waiterKPI = getWaiterKPI(start, end);
        PeriodSummary summary = getPeriodSummary(start, end);

        String bestSelling = dishStats.stream()
                .max(Comparator.comparingInt(DishMarginStats::timesSold))
                .map(DishMarginStats::dishName)
                .orElse("—");
        String mostProfitable = dishStats.stream()
                .max(Comparator.comparing(DishMarginStats::grossProfit))
                .map(DishMarginStats::dishName)
                .orElse("—");
        String weakestMargin = dishStats.stream()
                .filter(d -> d.revenue().signum() > 0)
                .min(Comparator.comparing(DishMarginStats::marginPercent))
                .map(DishMarginStats::dishName)
                .orElse("—");
        String bestWaiter = waiterKPI.stream()
                .max(Comparator.comparing(WaiterKPI::totalProfit))
                .map(WaiterKPI::waiterName)
                .orElse("—");

        BigDecimal avgOrderValue = summary.orderCount() > 0
                ? summary.revenue().divide(BigDecimal.valueOf(summary.orderCount()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new ManagementSummary(
                summary.revenue(),
                summary.profit(),
                bestSelling,
                mostProfitable,
                weakestMargin,
                bestWaiter,
                summary.cost(),
                avgOrderValue
        );
    }

    @Override
    public Map<Long, BigDecimal> getProfitByWaiter(LocalDate start, LocalDate end) {
        Map<Long, BigDecimal> result = new HashMap<>();
        for (WaiterKPI kpi : getWaiterKPI(start, end)) {
            result.put(kpi.waiterId(), kpi.totalProfit());
        }
        return result;
    }

    private String getWaiterName(Long waiterId) {
        if (waiterId == null || employeeStorageService == null) return "Waiter #" + waiterId;
        var e = employeeStorageService.getEmployeeById(waiterId);
        return e != null && e.getFullName() != null ? e.getFullName() : "Waiter #" + waiterId;
    }

    private static class DishAgg {
        String name;
        int timesSold;
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;

        DishAgg(String name) { this.name = name != null ? name : "Unknown"; }
    }

    private static class WaiterAgg {
        String name;
        int ordersHandled;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        WaiterAgg(String name) { this.name = name != null ? name : "Unknown"; }
    }
}
