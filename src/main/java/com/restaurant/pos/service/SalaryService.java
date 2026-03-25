package com.restaurant.pos.service;

import java.math.BigDecimal;

/**
 * Service for employee salary management.
 * Calculates salary based on worked hours, hourly rate, bonuses, and penalties.
 */
public interface SalaryService {

    /**
     * Calculate salary for employee.
     * Formula: salary = workedHours * hourlyRate + bonuses - penalties
     * 
     * @param employeeId employee ID
     * @return calculated salary or BigDecimal.ZERO if employee not found
     */
    BigDecimal calculateSalary(Long employeeId);

    /**
     * Apply bonus to employee's salary balance.
     * Adds positive amount to balance.
     * 
     * @param employeeId employee ID
     * @param amount bonus amount (must be positive)
     * @param comment reason/description for bonus
     * @return true if successful, false if employee not found or invalid amount
     */
    boolean applyBonus(Long employeeId, BigDecimal amount, String comment);

    /**
     * Apply penalty to employee's salary balance.
     * Subtracts amount from balance (amount should be positive, will be subtracted).
     * 
     * @param employeeId employee ID
     * @param amount penalty amount (must be positive)
     * @param comment reason/description for penalty
     * @return true if successful, false if employee not found or invalid amount
     */
    boolean applyPenalty(Long employeeId, BigDecimal amount, String comment);

    /**
     * Get current salary balance for employee.
     * Balance = calculated salary + bonuses - penalties
     * 
     * @param employeeId employee ID
     * @return current salary balance or BigDecimal.ZERO if employee not found
     */
    BigDecimal getSalaryBalance(Long employeeId);

    /**
     * Reset salary balance for employee.
     * Typically called after salary payment.
     * 
     * @param employeeId employee ID
     * @return true if successful, false if employee not found
     */
    boolean resetSalaryBalance(Long employeeId);

    /**
     * Get total bonuses across all employees.
     * 
     * @return total bonuses amount
     */
    BigDecimal getTotalBonuses();

    /**
     * Get total penalties across all employees.
     * 
     * @return total penalties amount
     */
    BigDecimal getTotalPenalties();
}
