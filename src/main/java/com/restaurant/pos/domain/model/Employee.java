package com.restaurant.pos.domain.model;

import com.restaurant.pos.domain.enums.Role;

import java.math.BigDecimal;

public class Employee {

    private Long id;
    private String name;
    private String fullName; // Kept for backward compatibility
    private int age;
    private String phone;
    private String address;
    private Role role;
    private boolean active;
    private BigDecimal hourlyRate;
    private double workedHours;
    private BigDecimal salaryBalance;
    private String login;
    private String passwordHash;

    public Employee() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : fullName;
    }

    public void setName(String name) {
        this.name = name;
        // Also update fullName for backward compatibility
        if (fullName == null) {
            this.fullName = name;
        }
    }

    public String getFullName() {
        return fullName != null ? fullName : name;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        // Also update name for consistency
        if (name == null) {
            this.name = fullName;
        }
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public BigDecimal getSalaryBalance() {
        return salaryBalance;
    }

    public void setSalaryBalance(BigDecimal salaryBalance) {
        this.salaryBalance = salaryBalance;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}

