package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.SalaryTransactionType;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.SalaryTransaction;
import com.restaurant.pos.service.PayrollService;
import com.restaurant.pos.service.TimeTrackingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SimplePayrollService implements PayrollService {

    private final TimeTrackingService timeTrackingService;
    private final Map<Long, Employee> employeesById;
    private final List<SalaryTransaction> transactions;

    public SimplePayrollService(TimeTrackingService timeTrackingService,
                                Map<Long, Employee> employeesById,
                                List<SalaryTransaction> transactions) {
        this.timeTrackingService = timeTrackingService;
        this.employeesById = employeesById;
        this.transactions = transactions;
    }

    @Override
    public BigDecimal calculateSalary(Long employeeId, LocalDateTime start, LocalDateTime end) {
        Employee employee = employeesById.get(employeeId);
        if (employee == null || employee.getHourlyRate() == null) {
            return BigDecimal.ZERO;
        }
        double hours = timeTrackingService.getWorkedHours(employeeId, start, end);
        BigDecimal hoursValue = BigDecimal.valueOf(hours);
        BigDecimal amount = employee.getHourlyRate().multiply(hoursValue);

        SalaryTransaction tx = new SalaryTransaction();
        tx.setEmployeeId(employeeId);
        tx.setAmount(amount);
        tx.setType(SalaryTransactionType.SALARY);
        tx.setTimestamp(LocalDateTime.now());
        tx.setComment("Salary for period");
        transactions.add(tx);

        return amount;
    }

    @Override
    public void applyBonus(Long employeeId, BigDecimal amount, String reason) {
        if (employeeId == null || amount == null || amount.signum() <= 0) {
            return;
        }
        SalaryTransaction tx = new SalaryTransaction();
        tx.setEmployeeId(employeeId);
        tx.setAmount(amount);
        tx.setType(SalaryTransactionType.BONUS);
        tx.setTimestamp(LocalDateTime.now());
        tx.setComment(reason);
        transactions.add(tx);
    }

    @Override
    public void applyPenalty(Long employeeId, BigDecimal amount, String reason) {
        if (employeeId == null || amount == null || amount.signum() <= 0) {
            return;
        }
        SalaryTransaction tx = new SalaryTransaction();
        tx.setEmployeeId(employeeId);
        tx.setAmount(amount.negate());
        tx.setType(SalaryTransactionType.PENALTY);
        tx.setTimestamp(LocalDateTime.now());
        tx.setComment(reason);
        transactions.add(tx);
    }
}

