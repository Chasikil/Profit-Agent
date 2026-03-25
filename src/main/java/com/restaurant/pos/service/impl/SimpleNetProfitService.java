package com.restaurant.pos.service.impl;

import com.restaurant.pos.service.FinanceReportService;
import com.restaurant.pos.service.NetProfitService;
import com.restaurant.pos.service.SalaryReportService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SimpleNetProfitService implements NetProfitService {

    private final FinanceReportService financeReportService;
    private final SalaryReportService salaryReportService;

    public SimpleNetProfitService(FinanceReportService financeReportService,
                                  SalaryReportService salaryReportService) {
        this.financeReportService = financeReportService;
        this.salaryReportService = salaryReportService;
    }

    @Override
    public BigDecimal calculateNetProfit(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return BigDecimal.ZERO;
        }
        BigDecimal profit = financeReportService.getProfitByPeriod(start, end);
        BigDecimal salaryExpenses = salaryReportService.getSalaryExpenses(start, end);
        return profit.subtract(salaryExpenses);
    }
}
