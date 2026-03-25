package com.restaurant.pos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SalaryReportService {

    BigDecimal getSalaryExpenses(LocalDateTime start, LocalDateTime end);

    BigDecimal getEmployeeSalary(Long employeeId, LocalDateTime start, LocalDateTime end);
}

