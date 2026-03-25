package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Employee;

import java.util.List;
import java.util.Map;

/**
 * Service for storing and accessing employees.
 * Provides shared in-memory storage for employee data.
 */
public interface EmployeeStorageService {

    /**
     * Get employee by ID.
     * 
     * @param employeeId employee ID
     * @return employee or null if not found
     */
    Employee getEmployeeById(Long employeeId);

    /**
     * Get all employees.
     * 
     * @return list of all employees
     */
    List<Employee> getAllEmployees();

    /**
     * Add or update employee.
     * 
     * @param employee employee to add/update
     */
    void saveEmployee(Employee employee);

    /**
     * Get internal storage map (for services that need direct access).
     *
     * @return map of employee ID to Employee
     */
    Map<Long, Employee> getEmployeesMap();

    /**
     * Seed default employees if storage is empty.
     * Creates: Ivan, Anna, Maria (WAITER), Admin (ADMIN). No duplicates on restart.
     */
    void seedDefaultEmployees();
}
