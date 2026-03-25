package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.service.*;
import com.restaurant.pos.ui.view.AnalyticsView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Analytics view (ADMIN only).
 * Period-based profit analytics, margin analysis, waiter KPI.
 */
public class AnalyticsController {

    private final BIAnalyticsService biAnalyticsService;
    private final EmployeeStorageService employeeStorageService;
    private final AnalyticsService analyticsService;
    private final ReportExportService reportExportService;
    private AnalyticsView view;

    public AnalyticsController(BIAnalyticsService biAnalyticsService,
                               EmployeeStorageService employeeStorageService,
                               AnalyticsService analyticsService,
                               ReportExportService reportExportService) {
        this.biAnalyticsService = biAnalyticsService;
        this.employeeStorageService = employeeStorageService;
        this.analyticsService = analyticsService;
        this.reportExportService = reportExportService;
    }

    public AnalyticsView getView() {
        if (view == null) {
            view = new AnalyticsView(this);
            if (analyticsService != null) {
                view.setAnalyticsService(analyticsService);
            }
        }
        return view;
    }

    public void refresh() {
        if (view != null) {
            view.refresh();
        }
    }

    public enum PeriodType { TODAY, THIS_WEEK, THIS_MONTH, CUSTOM }

    public LocalDate[] getPeriodRange(PeriodType type, LocalDate customStart, LocalDate customEnd) {
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end = today;
        switch (type) {
            case TODAY:
                start = today;
                break;
            case THIS_WEEK:
                start = today.minusDays(today.getDayOfWeek().getValue() - 1);
                break;
            case THIS_MONTH:
                start = today.withDayOfMonth(1);
                break;
            case CUSTOM:
                start = customStart != null ? customStart : today;
                end = customEnd != null ? customEnd : today;
                if (start.isAfter(end)) {
                    LocalDate t = start; start = end; end = t;
                }
                break;
            default:
                start = today;
        }
        return new LocalDate[]{ start, end };
    }

    public BIAnalyticsService.PeriodSummary getPeriodSummary(LocalDate start, LocalDate end) {
        return biAnalyticsService != null ? biAnalyticsService.getPeriodSummary(start, end) : null;
    }

    public List<BIAnalyticsService.DishMarginStats> getDishMarginStats(LocalDate start, LocalDate end) {
        return biAnalyticsService != null ? biAnalyticsService.getDishMarginStats(start, end) : List.of();
    }

    public List<BIAnalyticsService.WaiterKPI> getWaiterKPI(LocalDate start, LocalDate end) {
        return biAnalyticsService != null ? biAnalyticsService.getWaiterKPI(start, end) : List.of();
    }

    public String exportToExcel(LocalDate start, LocalDate end) {
        if (reportExportService == null) return null;
        try {
            return reportExportService.exportToExcel(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    public String getWaiterName(Long waiterId) {
        if (waiterId == null || employeeStorageService == null) return "Waiter #" + waiterId;
        Employee e = employeeStorageService.getEmployeeById(waiterId);
        return e != null && e.getFullName() != null ? e.getFullName() : "Waiter #" + waiterId;
    }
}
