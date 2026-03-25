package com.restaurant.pos.service;

import com.restaurant.pos.ui.model.InventoryReport;
import com.restaurant.pos.ui.model.SalaryReport;
import com.restaurant.pos.ui.model.SalesReport;

/**
 * Service for generating financial reports.
 * All calculations are performed inside this service.
 */
public interface ReportService {

    /**
     * Build sales report with revenue, cost, profit, and margin.
     * 
     * @return SalesReport DTO with calculated metrics
     */
    SalesReport buildSalesReport();

    /**
     * Build inventory report with stock value and write-offs.
     * 
     * @return InventoryReport DTO with calculated metrics
     */
    InventoryReport buildInventoryReport();

    /**
     * Build salary report with payroll, bonuses, and penalties.
     * 
     * @return SalaryReport DTO with calculated metrics
     */
    SalaryReport buildSalaryReport();
}
