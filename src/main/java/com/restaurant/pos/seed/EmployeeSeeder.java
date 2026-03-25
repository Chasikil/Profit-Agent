package com.restaurant.pos.seed;

import com.restaurant.pos.domain.enums.Role;
import com.restaurant.pos.domain.model.Employee;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeder for default employees.
 * Creates initial employees for the application on startup.
 */
public final class EmployeeSeeder {

    private EmployeeSeeder() {
        // utility class
    }

    /**
     * Create default employees:
     * ADMIN: Кристина Орлова (admin / admin123)
     * WAITER: Иван Петров (ivan / 1234), Анна Смирнова (anna / 1234), Мария Кузнецова (maria / 1234)
     */
    public static List<Employee> createDefaultEmployees() {
        List<Employee> employees = new ArrayList<>();

        employees.add(createEmployee(1L, "````````````````````````````````````````````````````````````Иван Петров", "Иван Петров", Role.WAITER, "ivan", "1234"));
        employees.add(createEmployee(2L, "Анна Смирнова", "Анна Смирнова", Role.WAITER, "anna", "1234"));
        employees.add(createEmployee(3L, "Мария Кузнецова", "Мария Кузнецова", Role.WAITER, "maria", "1234"));
        employees.add(createEmployee(4L, "Кристина Орлова", "Кристина Орлова", Role.ADMIN, "admin", "admin123"));

        return employees;
    }

    private static Employee createEmployee(Long id,
                                           String name,
                                           String fullName,
                                           Role role,
                                           String login,
                                           String rawPassword) {
        Employee e = new Employee();
        e.setId(id);
        e.setName(name);
        e.setFullName(fullName);
        e.setRole(role);
        e.setLogin(login);
        e.setPasswordHash(rawPassword);
        e.setActive(true);
        e.setHourlyRate(BigDecimal.ZERO);
        e.setSalaryBalance(BigDecimal.ZERO);
        e.setWorkedHours(0.0);
        return e;
    }
}

