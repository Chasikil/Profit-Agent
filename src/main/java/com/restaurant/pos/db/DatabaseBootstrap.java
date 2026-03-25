package com.restaurant.pos.db;

import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.seed.EmployeeSeeder;

import java.util.List;

/**
 * Bootstraps database: initializes schema, loads data on startup.
 */
public class DatabaseBootstrap {

    public static void ensureSchema() {
        DatabaseManager.getInstance();
        ensureDefaultUsers();
    }

    private static void ensureDefaultUsers() {
        EmployeeRepository repo = new EmployeeRepository();
        if (repo.count() > 0) {
            return;
        }
        List<Employee> defaults = EmployeeSeeder.createDefaultEmployees();
        for (Employee e : defaults) {
            repo.save(e);
        }
    }
}
