package com.restaurant.pos.domain.model;

import com.restaurant.pos.domain.enums.SalaryTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalaryTransaction {

    private Long employeeId;
    private BigDecimal amount;
    private SalaryTransactionType type;
    private LocalDateTime timestamp;
    private String comment;

    public SalaryTransaction() {
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public SalaryTransactionType getType() {
        return type;
    }

    public void setType(SalaryTransactionType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

