package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.SalaryTransaction;
import com.restaurant.pos.service.SalaryReportService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class InMemorySalaryReportService implements SalaryReportService {

    private final List<SalaryTransaction> transactions;

    public InMemorySalaryReportService(List<SalaryTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public BigDecimal getSalaryExpenses(LocalDateTime start, LocalDateTime end) {
        return sumTransactions(null, start, end);
    }

    @Override
    public BigDecimal getEmployeeSalary(Long employeeId, LocalDateTime start, LocalDateTime end) {
        return sumTransactions(employeeId, start, end);
    }

    private BigDecimal sumTransactions(Long employeeId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (SalaryTransaction tx : transactions) {
            if (employeeId != null && !employeeId.equals(tx.getEmployeeId())) {
                continue;
            }
            LocalDateTime ts = tx.getTimestamp();
            if (ts == null || ts.isBefore(start) || ts.isAfter(end)) {
                continue;
            }
            if (tx.getAmount() != null) {
                total = total.add(tx.getAmount());
            }
        }
        return total;
    }
}

