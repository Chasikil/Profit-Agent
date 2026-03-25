package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.enums.OrderStatus;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Order;
import com.restaurant.pos.service.AnalyticsService;
import com.restaurant.pos.service.BIAnalyticsService;
import com.restaurant.pos.service.DashboardService;
import com.restaurant.pos.service.InventoryService;
import com.restaurant.pos.service.ShiftService;
import com.restaurant.pos.service.TableService;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.service.FinanceService;
import com.restaurant.pos.service.OrderPricingService;
import com.restaurant.pos.service.OrderService;
import com.restaurant.pos.ui.model.OperationRowDTO;
import com.restaurant.pos.ui.view.DashboardView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    private final DashboardService dashboardService;
    private final OrderService orderService;
    private final OrderPricingService orderPricingService;
    private final EmployeeStorageService employeeStorageService;
    private final AnalyticsService analyticsService;
    private final FinanceService financeService;
    private final ShiftService shiftService;
    private final TableService tableService;
    private final InventoryService inventoryService;
    private final BIAnalyticsService biAnalyticsService;
    private DashboardView dashboardView;

    public DashboardController(DashboardService dashboardService,
                               OrderService orderService,
                               OrderPricingService orderPricingService,
                               EmployeeStorageService employeeStorageService,
                               AnalyticsService analyticsService,
                               FinanceService financeService,
                               ShiftService shiftService,
                               TableService tableService,
                               InventoryService inventoryService,
                               BIAnalyticsService biAnalyticsService) {
        this.dashboardService = dashboardService;
        this.orderService = orderService;
        this.orderPricingService = orderPricingService;
        this.employeeStorageService = employeeStorageService;
        this.analyticsService = analyticsService;
        this.financeService = financeService;
        this.shiftService = shiftService;
        this.tableService = tableService;
        this.inventoryService = inventoryService;
        this.biAnalyticsService = biAnalyticsService;
    }

    public DashboardView getView() {
        if (dashboardView == null) {
            dashboardView = new DashboardView();
            if (shiftService != null) {
                dashboardView.setOnCloseShift(this::handleCloseShift);
            }
        }
        loadDashboardData();
        return dashboardView;
    }

    private void handleCloseShift() {
        if (shiftService == null) return;
        com.restaurant.pos.model.ShiftModel activeShift = shiftService.getActiveRestaurantShift();
        if (activeShift == null) {
            dashboardView.showError("No active shift to close.");
            return;
        }
        Long shiftId = activeShift.getId();
        ShiftService.ShiftSummary summary = shiftService.closeRestaurantShift();
        String msg = String.format("Shift closed.\nRevenue: %s\nCost: %s\nProfit: %s\nOrders: %d\nCash: %s\nCard: %s",
                formatCurrency(summary.totalRevenue), formatCurrency(summary.totalCost), formatCurrency(summary.totalProfit),
                summary.orderCount, formatCurrency(summary.cashTotal), formatCurrency(summary.cardTotal));
        dashboardView.showInfo("Shift Summary", msg);
        com.restaurant.pos.model.ShiftModel closedShift = shiftService.getShiftById(shiftId);
        if (closedShift != null) {
            new Thread(() -> {
                try {
                    com.restaurant.pos.util.ShiftReportGenerator.generateShiftReport(summary, closedShift, financeService, orderPricingService);
                } catch (Exception e) {
                    System.err.println("Failed to generate shift report: " + e.getMessage());
                }
            }).start();
        }
        refresh();
    }

    private void loadDashboardData() {
        try {
            // Загрузка today's statistics
            loadTodayStats();

            // Загрузка revenue chart data
            loadRevenueChartData();
            
            // Загрузка orders chart data
            loadOrdersChartData();

            // Загрузка KPI из DashboardService
            loadKpiMetrics();

            // Загрузка последних операций
            loadRecentOperations();
        } catch (Exception e) {
            dashboardView.showError("Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    /**
     * Load today's statistics and update view.
     */
    private void loadTodayStats() {
        try {
            LocalDate today = LocalDate.now();
            
            // Get today's orders count
            int ordersToday = 0;
            if (analyticsService != null) {
                Map<LocalDate, Integer> ordersCountPerDay = analyticsService.getOrdersCountPerDay(today, today);
                ordersToday = ordersCountPerDay.getOrDefault(today, 0);
            }

            // Get today's revenue
            BigDecimal revenueToday = BigDecimal.ZERO;
            if (analyticsService != null) {
                Map<LocalDate, BigDecimal> revenuePerDay = analyticsService.getRevenuePerDay(today, today);
                revenueToday = revenuePerDay.getOrDefault(today, BigDecimal.ZERO);
            }

            // Calculate average order value
            BigDecimal averageOrderValue = BigDecimal.ZERO;
            if (ordersToday > 0 && revenueToday != null && revenueToday.signum() > 0) {
                averageOrderValue = revenueToday.divide(BigDecimal.valueOf(ordersToday), 2, RoundingMode.HALF_UP);
            }

            // Get today's net profit
            BigDecimal netProfitToday = BigDecimal.ZERO;
            if (analyticsService != null) {
                Map<LocalDate, BigDecimal> profitPerDay = analyticsService.getProfitPerDay(today, today);
                netProfitToday = profitPerDay.getOrDefault(today, BigDecimal.ZERO);
            }

            dashboardView.setTodayStats(revenueToday, ordersToday, averageOrderValue, netProfitToday);

            if (shiftService != null) {
                var activeShift = shiftService.getActiveRestaurantShift();
                if (activeShift != null) {
                    dashboardView.setShiftStats(activeShift.getTotalRevenue(), activeShift.getTotalProfit());
                } else {
                    dashboardView.setShiftStats(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO);
                }
            }
            if (tableService != null) {
                int free = tableService.getFreeTables().size();
                int total = tableService.getAllTables().size();
                dashboardView.setOpenTables(free, total);
            }

            if (inventoryService != null) {
                java.util.List<com.restaurant.pos.domain.model.Product> lowStock = inventoryService.getLowStockIngredients();
                dashboardView.setLowStockIngredients(lowStock);
            }

            if (biAnalyticsService != null) {
                BIAnalyticsService.ManagementSummary mgmt = biAnalyticsService.getManagementSummary(today, today);
                dashboardView.setManagementSummary(mgmt);
            }
        } catch (Exception e) {
            dashboardView.showError("Ошибка при загрузке статистики за сегодня: " + e.getMessage());
        }
    }

    /**
     * Load revenue chart data for last 7 days.
     */
    private void loadRevenueChartData() {
        try {
            if (analyticsService == null) {
                return;
            }

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(6); // Last 7 days including today

            Map<LocalDate, BigDecimal> revenuePerDay = analyticsService.getRevenuePerDay(startDate, today);
            dashboardView.setRevenueChartData(revenuePerDay);
        } catch (Exception e) {
            dashboardView.showError("Ошибка при загрузке данных графика: " + e.getMessage());
        }
    }

    /**
     * Load orders chart data for last 7 days.
     */
    private void loadOrdersChartData() {
        try {
            if (analyticsService == null) {
                return;
            }

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(6); // Last 7 days including today

            Map<LocalDate, Integer> ordersCountPerDay = analyticsService.getOrdersCountPerDay(startDate, today);
            dashboardView.setOrdersChartData(ordersCountPerDay);
        } catch (Exception e) {
            dashboardView.showError("Ошибка при загрузке данных графика заказов: " + e.getMessage());
        }
    }

    /**
     * Refresh dashboard data.
     * Called when orders are created to auto-refresh statistics.
     */
    public void refresh() {
        if (dashboardView != null) {
            loadDashboardData();
        }
    }

    private void loadKpiMetrics() {
        try {
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalProfit = BigDecimal.ZERO;
            int paidOrdersCount = 0;

            if (financeService != null) {
                totalRevenue = financeService.getTotalRevenueFromOrders();
                if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
                totalProfit = financeService.getTotalProfitFromOrders();
                if (totalProfit == null) totalProfit = BigDecimal.ZERO;
            }

            if (orderService != null) {
                List<Order> allOrders = orderService.getAllOrders();
                if (allOrders != null) {
                    for (Order o : allOrders) {
                        if (o != null && o.getStatus() == OrderStatus.PAID) {
                            paidOrdersCount++;
                            if (financeService == null || (totalRevenue.signum() == 0 && totalProfit.signum() == 0)) {
                                BigDecimal rev = orderPricingService != null ? orderPricingService.calculateTotal(o) : BigDecimal.ZERO;
                                totalRevenue = totalRevenue.add(rev != null ? rev : BigDecimal.ZERO);
                                BigDecimal profit = o.getTotalProfit();
                                totalProfit = totalProfit.add(profit != null ? profit : BigDecimal.ZERO);
                            }
                        }
                    }
                }
            }

            BigDecimal marginPercent = BigDecimal.ZERO;
            if (totalRevenue != null && totalRevenue.signum() > 0 && totalProfit != null) {
                marginPercent = totalProfit.multiply(BigDecimal.valueOf(100)).divide(totalRevenue, 2, RoundingMode.HALF_UP);
            }

            dashboardView.setKpiValues(formatCurrency(totalRevenue), formatCurrency(totalProfit), formatPercent(marginPercent));
            dashboardView.setOrderSummary(paidOrdersCount, formatCurrency(totalRevenue));
        } catch (Exception e) {
            dashboardView.showError("Ошибка при загрузке KPI метрик: " + e.getMessage());
        }
    }

    private void loadRecentOperations() {
        try {
            // Получение последних заказов (операций)
            List<Order> orders = orderService.getAllOrders();
            if (orders == null) {
                orders = new ArrayList<>();
            }

            // Фильтрация только оплаченных заказов и сортировка по дате (новые первыми)
            List<Order> paidOrders = orders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.PAID)
                    .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(20) // Последние 20 операций
                    .collect(Collectors.toList());

            // Преобразование в DTO
            List<OperationRowDTO> operationDTOs = new ArrayList<>();
            for (Order order : paidOrders) {
                OperationRowDTO dto = convertOrderToOperationDTO(order);
                if (dto != null) {
                    operationDTOs.add(dto);
                }
            }

            dashboardView.setOperations(operationDTOs);
        } catch (Exception e) {
            dashboardView.showError("Ошибка при загрузке операций: " + e.getMessage());
        }
    }

    private OperationRowDTO convertOrderToOperationDTO(Order order) {
        if (order == null) {
            return null;
        }

        OperationRowDTO dto = new OperationRowDTO();

        // Время операции
        dto.setTime(order.getCreatedAt());

        // Тип операции
        dto.setOperationType("Заказ");

        // Сумма заказа
        BigDecimal amount = orderPricingService.calculateTotal(order);
        dto.setAmount(amount != null ? amount : BigDecimal.ZERO);

        // Имя сотрудника
        String employeeName = getEmployeeName(order.getWaiterId());
        dto.setEmployee(employeeName != null ? employeeName : "Неизвестно");

        return dto;
    }

    private String getEmployeeName(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        try {
            Employee employee = employeeStorageService.getEmployeeById(employeeId);
            return employee != null ? employee.getFullName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        NumberFormat formatter = new DecimalFormat("#,##0.00");
        return "₽ " + formatter.format(value);
    }

    private String formatPercent(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        NumberFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(value) + "%";
    }
}
