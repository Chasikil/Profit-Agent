package com.restaurant.pos.service.impl;

import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.domain.model.Session;
import com.restaurant.pos.service.AuthService;
import com.restaurant.pos.service.SessionService;
import com.restaurant.pos.seed.EmployeeSeeder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryAuthService implements AuthService {

    private final Map<String, Employee> employeesByLogin = new HashMap<>();
    private final SessionService sessionService;

    public InMemoryAuthService(SessionService sessionService) {
        this.sessionService = sessionService;
        seedEmployees();
    }

    private void seedEmployees() {
        for (Employee employee : EmployeeSeeder.createDefaultEmployees()) {
            if (employee.getLogin() != null) {
                employeesByLogin.put(employee.getLogin(), employee);
            }
        }
    }

    @Override
    public Session login(String login, String password) {
        Employee employee = employeesByLogin.get(login);
        if (employee == null) {
            return null;
        }
        if (password == null || !password.equals(employee.getPasswordHash())) {
            return null;
        }
        return sessionService.startSession(employee);
    }

    @Override
    public void logout() {
        sessionService.endSession();
    }

    @Override
    public Session getCurrentSession() {
        return sessionService.getCurrentSession();
    }

    @Override
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeesByLogin.values());
    }
}

