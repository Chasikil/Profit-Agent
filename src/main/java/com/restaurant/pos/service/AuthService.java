package com.restaurant.pos.service;

import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Session;

import java.util.List;

public interface AuthService {

    Session login(String login, String password);

    void logout();

    Session getCurrentSession();

    /**
     * Get all employees (for syncing with EmployeeStorageService).
     * 
     * @return list of all employees
     */
    List<Employee> getAllEmployees();
}

