package com.restaurant.pos.ui.model;

import java.math.BigDecimal;

/**
 * DTO for salary report.
 * Contains payroll financial metrics.
 */
public class SalaryReport {

    private BigDecimal totalPayroll;
    private BigDecimal totalBonuses;
    private BigDecimal totalPenalties;

    public SalaryReport() {
        this.totalPayroll = BigDecimal.ZERO;
        this.totalBonuses = BigDecimal.ZERO;
        this.totalPenalties = BigDecimal.ZERO;
    }

    public BigDecimal getTotalPayroll() {
        return totalPayroll;
    }

    public void setTotalPayroll(BigDecimal totalPayroll) {
        this.totalPayroll = totalPayroll != null ? totalPayroll : BigDecimal.ZERO;
    }

    public BigDecimal getTotalBonuses() {
        return totalBonuses;
    }

    public void setTotalBonuses(BigDecimal totalBonuses) {
        this.totalBonuses = totalBonuses != null ? totalBonuses : BigDecimal.ZERO;
    }

    public BigDecimal getTotalPenalties() {
        return totalPenalties;
    }

    public void setTotalPenalties(BigDecimal totalPenalties) {
        this.totalPenalties = totalPenalties != null ? totalPenalties : BigDecimal.ZERO;
    }
}
