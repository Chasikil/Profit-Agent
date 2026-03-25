package com.restaurant.pos.model;

import java.math.BigDecimal;

/**
 * Simple employee model for application/service layer (no UI logic).
 * Contains only fields required for dashboards and salary calculations.
 */
public class EmployeeModel {

    private String fullName;
    private String role;
    private BigDecimal hourlyRate;
    private double workedHours;

    public EmployeeModel() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(double workedHours) {
        this.workedHours = workedHours;
    }
}

