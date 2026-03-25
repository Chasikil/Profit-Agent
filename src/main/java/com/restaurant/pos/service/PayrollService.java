package com.restaurant.pos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PayrollService {

    BigDecimal calculateSalary(Long employeeId, LocalDateTime start, LocalDateTime end);

    void applyBonus(Long employeeId, BigDecimal amount, String reason);

    void applyPenalty(Long employeeId, BigDecimal amount, String reason);
}

