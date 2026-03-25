package com.restaurant.pos.ui.controller;

import com.restaurant.pos.domain.model.Employee;
import com.restaurant.pos.service.EmployeeStorageService;
import com.restaurant.pos.service.SalaryService;
import com.restaurant.pos.ui.context.SessionContext;
import com.restaurant.pos.ui.view.SalaryView;

import java.math.BigDecimal;
import java.util.List;

/**
 * MVC controller for manager salary operations.
 * 
 * UI -> SalaryController -> EmployeeStorageService / SalaryService
 */
public class SalaryController {

    private final EmployeeStorageService employeeStorageService;
    private final SalaryService salaryService;
    private final SessionContext sessionContext;

    private SalaryView salaryView;

    public SalaryController(EmployeeStorageService employeeStorageService,
                            SalaryService salaryService,
                            SessionContext sessionContext) {
        this.employeeStorageService = employeeStorageService;
        this.salaryService = salaryService;
        this.sessionContext = sessionContext;
    }

    public SalaryView getView() {
        if (salaryView == null) {
            salaryView = new SalaryView(this);
        }
        if (!checkAccess()) {
            salaryView.showError("Доступ запрещён. Только менеджер или директор могут работать с зарплатами.");
            return salaryView;
        }
        loadEmployees();
        return salaryView;
    }

    /**
     * Load all employees and refresh salary data.
     */
    public void loadEmployees() {
        if (salaryView == null) {
            return;
        }
        if (!checkAccess()) {
            return;
        }

        List<Employee> employees = employeeStorageService.getAllEmployees();
        
        // Calculate salary for each employee before displaying
        for (Employee employee : employees) {
            if (employee != null && employee.getId() != null) {
                salaryService.calculateSalary(employee.getId());
            }
        }

        salaryView.updateTable(employees);
    }

    /**
     * Apply bonus to selected employee.
     */
    public void handleApplyBonus(Employee employee, BigDecimal amount, String comment) {
        if (!checkAccess()) {
            showError("Доступ запрещён. Только менеджер или директор могут работать с зарплатами.");
            return;
        }

        if (employee == null || employee.getId() == null) {
            showError("Не выбран сотрудник.");
            return;
        }

        if (amount == null || amount.signum() <= 0) {
            showError("Сумма бонуса должна быть положительной.");
            return;
        }

        boolean success = salaryService.applyBonus(employee.getId(), amount, comment);
        if (success) {
            loadEmployees(); // Refresh to show updated balance
        } else {
            showError("Не удалось применить бонус.");
        }
    }

    /**
     * Apply penalty to selected employee.
     */
    public void handleApplyPenalty(Employee employee, BigDecimal amount, String comment) {
        if (!checkAccess()) {
            showError("Доступ запрещён. Только менеджер или директор могут работать с зарплатами.");
            return;
        }

        if (employee == null || employee.getId() == null) {
            showError("Не выбран сотрудник.");
            return;
        }

        if (amount == null || amount.signum() <= 0) {
            showError("Сумма штрафа должна быть положительной.");
            return;
        }

        boolean success = salaryService.applyPenalty(employee.getId(), amount, comment);
        if (success) {
            loadEmployees(); // Refresh to show updated balance
        } else {
            showError("Не удалось применить штраф.");
        }
    }

    /**
     * Check if current user has access (MANAGER or DIRECTOR).
     */
    private boolean checkAccess() {
        if (sessionContext == null) {
            return false;
        }
        SessionContext.Role role = sessionContext.getRole();
        return role == SessionContext.Role.MANAGER
                || role == SessionContext.Role.DIRECTOR;
    }

    private void showError(String message) {
        if (salaryView != null) {
            salaryView.showError(message);
        }
    }
}
