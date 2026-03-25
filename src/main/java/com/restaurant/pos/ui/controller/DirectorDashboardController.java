package com.restaurant.pos.ui.controller;

import com.restaurant.pos.service.ReportService;
import com.restaurant.pos.ui.context.SessionContext;
import com.restaurant.pos.ui.model.SalaryReport;
import com.restaurant.pos.ui.model.SalesReport;
import com.restaurant.pos.ui.view.DirectorDashboardView;

/**
 * MVC controller for director dashboard.
 * 
 * UI -> DirectorDashboardController -> ReportService
 */
public class DirectorDashboardController {

    private final ReportService reportService;
    private final SessionContext sessionContext;
    private DirectorDashboardView directorDashboardView;

    public DirectorDashboardController(ReportService reportService, SessionContext sessionContext) {
        this.reportService = reportService;
        this.sessionContext = sessionContext;
    }

    public DirectorDashboardView getView() {
        if (directorDashboardView == null) {
            directorDashboardView = new DirectorDashboardView(this);
        }
        if (!checkAccess()) {
            directorDashboardView.showError("Доступ запрещён. Только директор может просматривать финансовую панель.");
            return directorDashboardView;
        }
        loadReports();
        return directorDashboardView;
    }

    /**
     * Load all reports and update view.
     */
    public void loadReports() {
        if (directorDashboardView == null) {
            return;
        }
        if (!checkAccess()) {
            return;
        }

        // Build all reports
        SalesReport salesReport = reportService.buildSalesReport();
        SalaryReport salaryReport = reportService.buildSalaryReport();

        // Update view with report data
        directorDashboardView.updateSalesData(
                salesReport.getTotalRevenue(),
                salesReport.getTotalCost(),
                salesReport.getGrossProfit(),
                salesReport.getMarginPercent()
        );
        directorDashboardView.updatePayrollData(salaryReport.getTotalPayroll());
    }

    /**
     * Refresh reports (reload data).
     */
    public void refreshReports() {
        loadReports();
    }

    /**
     * Check if current user has access (DIRECTOR only).
     */
    private boolean checkAccess() {
        if (sessionContext == null) {
            return false;
        }
        SessionContext.Role role = sessionContext.getRole();
        return role == SessionContext.Role.DIRECTOR;
    }
}
