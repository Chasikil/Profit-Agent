package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.seed.EmployeeSeeder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of EmployeeStorageService.
 * Provides shared storage for employee data.
 */
public class InMemoryEmployeeStorageService implements EmployeeStorageService {

    private final Map<Long, Employee> employeesById = new HashMap<>();

    @Override
    public Employee getEmployeeById(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeesById.get(employeeId);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeesById.values());
    }

    @Override
    public void saveEmployee(Employee employee) {
        if (employee == null || employee.getId() == null) {
            return;
        }
        employeesById.put(employee.getId(), employee);
    }

    @Override
    public Map<Long, Employee> getEmployeesMap() {
        return employeesById;
    }

    @Override
    public void seedDefaultEmployees() {
        if (!employeesById.isEmpty()) {
            return;
        }
        for (Employee e : EmployeeSeeder.createDefaultEmployees()) {
            // Keep IDs from seeder to stay consistent across the app.
            saveEmployee(e);
        }
    }
}
