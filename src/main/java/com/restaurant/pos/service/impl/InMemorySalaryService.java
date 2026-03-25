package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.service.SalaryService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of SalaryService.
 * Calculates salary and manages bonuses/penalties using Employee model.
 */
public class InMemorySalaryService implements SalaryService {

    private final EmployeeStorageService employeeStorage;
    private final Map<Long, BigDecimal> bonuses = new HashMap<>();
    private final Map<Long, BigDecimal> penalties = new HashMap<>();

    public InMemorySalaryService(EmployeeStorageService employeeStorage) {
        if (employeeStorage == null) {
            throw new IllegalArgumentException("EmployeeStorageService cannot be null");
        }
        this.employeeStorage = employeeStorage;
    }

    @Override
    public BigDecimal calculateSalary(Long employeeId) {
        if (employeeId == null) {
            return BigDecimal.ZERO;
        }

        Employee employee = employeeStorage.getEmployeeById(employeeId);
        if (employee == null) {
            return BigDecimal.ZERO;
        }

        // Get worked hours and hourly rate
        double workedHours = employee.getWorkedHours();
        BigDecimal hourlyRate = employee.getHourlyRate();

        if (hourlyRate == null) {
            hourlyRate = BigDecimal.ZERO;
        }

        // Calculate base salary: workedHours * hourlyRate
        BigDecimal hoursValue = BigDecimal.valueOf(workedHours);
        BigDecimal baseSalary = hourlyRate.multiply(hoursValue);

        // Get bonuses and penalties
        BigDecimal bonusAmount = bonuses.getOrDefault(employeeId, BigDecimal.ZERO);
        BigDecimal penaltyAmount = penalties.getOrDefault(employeeId, BigDecimal.ZERO);

        // Formula: salary = workedHours * hourlyRate + bonuses - penalties
        BigDecimal salary = baseSalary.add(bonusAmount).subtract(penaltyAmount);

        // Update employee's salary balance
        employee.setSalaryBalance(salary);

        return salary;
    }

    @Override
    public boolean applyBonus(Long employeeId, BigDecimal amount, String comment) {
        if (employeeId == null || amount == null || amount.signum() <= 0) {
            return false;
        }

        Employee employee = employeeStorage.getEmployeeById(employeeId);
        if (employee == null) {
            return false;
        }

        // Add bonus to bonuses map
        BigDecimal currentBonus = bonuses.getOrDefault(employeeId, BigDecimal.ZERO);
        bonuses.put(employeeId, currentBonus.add(amount));

        // Recalculate salary balance
        calculateSalary(employeeId);

        return true;
    }

    @Override
    public boolean applyPenalty(Long employeeId, BigDecimal amount, String comment) {
        if (employeeId == null || amount == null || amount.signum() <= 0) {
            return false;
        }

        Employee employee = employeeStorage.getEmployeeById(employeeId);
        if (employee == null) {
            return false;
        }

        // Add penalty to penalties map
        BigDecimal currentPenalty = penalties.getOrDefault(employeeId, BigDecimal.ZERO);
        penalties.put(employeeId, currentPenalty.add(amount));

        // Recalculate salary balance
        calculateSalary(employeeId);

        return true;
    }

    @Override
    public BigDecimal getSalaryBalance(Long employeeId) {
        if (employeeId == null) {
            return BigDecimal.ZERO;
        }

        Employee employee = employeeStorage.getEmployeeById(employeeId);
        if (employee == null) {
            return BigDecimal.ZERO;
        }

        // Recalculate to ensure balance is up to date
        calculateSalary(employeeId);

        BigDecimal balance = employee.getSalaryBalance();
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Override
    public boolean resetSalaryBalance(Long employeeId) {
        if (employeeId == null) {
            return false;
        }

        Employee employee = employeeStorage.getEmployeeById(employeeId);
        if (employee == null) {
            return false;
        }

        // Reset worked hours, bonuses, and penalties
        employee.setWorkedHours(0.0);
        bonuses.remove(employeeId);
        penalties.remove(employeeId);
        employee.setSalaryBalance(BigDecimal.ZERO);

        return true;
    }

    @Override
    public BigDecimal getTotalBonuses() {
        return bonuses.values().stream()
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalPenalties() {
        return penalties.values().stream()
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
