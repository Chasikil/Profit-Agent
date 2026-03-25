package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Employee;

import java.util.List;

/**
 * Service for managing employees.
 * Provides business logic for employee operations.
 */
public interface EmployeeService {

    /**
     * Get all employees with WAITER role.
     * 
     * @return list of all waiters (active and inactive)
     */
    List<Employee> getAllWaiters();

    /**
     * Get all active employees with WAITER role.
     * 
     * @return list of active waiters only
     */
    List<Employee> getActiveWaiters();

    /**
     * Add a new employee.
     * Sets active = true by default.
     * 
     * @param employee employee to add
     * @return the added employee with assigned ID
     */
    Employee addEmployee(Employee employee);

    /**
     * Deactivate an employee.
     * Sets active = false.
     * 
     * @param employeeId ID of the employee to deactivate
     * @return true if employee was found and deactivated, false otherwise
     */
    boolean deactivateEmployee(Long employeeId);
}
